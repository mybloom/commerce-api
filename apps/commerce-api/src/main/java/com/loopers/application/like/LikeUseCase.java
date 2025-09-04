package com.loopers.application.like;

import com.loopers.application.common.PagingCondition;
import com.loopers.config.kafka.KafkaTopicsProperties;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.*;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.sharedkernel.LikeEvent;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.paging.PageableFactory;
import com.loopers.support.paging.Pagination;
import com.loopers.support.paging.PagingPolicy;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LikeUseCase {
    private final LikeService likeService;
    private final ProductService productService;
    private final BrandService brandService;
    private final LikeProductService likeProductService = new LikeProductService();
    private final ApplicationEventPublisher eventPublisher;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;

    private static final String TYPE_ID_HEADER = "__TypeId__";

    @Transactional
    public LikeResult.LikeRegisterResult register(final Long userId, final Long productId) {
        productService.retrieveOne(productId)
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "해당 상품에 좋아요를 할 수 없습니다."));

        //좋아요 등록
        LikeQuery.LikeRegisterQuery query = likeService.register(userId, productId);
        if (query.isDuplicatedRequest()) {
            return LikeResult.LikeRegisterResult.duplicated(userId, productId);
        }

        // 좋아요 수 증가(비동기)
        LikeEvent.LikeCountIncreased event = new LikeEvent.LikeCountIncreased(productId);
        eventPublisher.publishEvent(event);
/*

        // 토픽/키/값 지정
        ProducerRecord<String, Object> record =
                new ProducerRecord<>(topics.getLike(), null, event);

        // __TypeId__ 헤더에 FQCN 추가 (오타 방지: 클래스에서 가져오기 권장)
        String typeId = event.getClass().getName(); // "com.loopers.domain.sharedkernel.LikeEvent$LikeCountIncreased"
        record.headers().add(new RecordHeader("__TypeId__", typeId.getBytes(StandardCharsets.UTF_8)));

        //kafka -> todo: 이벤트타입이 "도메인 이벤트"가 되어야겠네..그래야 하나의 토픽으로 넣을 수 있쟎아. -> 그렇다면 왜 하나의 토픽으로 넣어야 될까? 내 생각을 정리하자
        kafkaTemplate.send(record);
*/

        String typeId = event.getClass().getName();

        Message<LikeEvent.LikeCountIncreased> msg = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "like-events")
                .setHeader("__TypeId__", typeId) // 타입 헤더 직접 지정
                .build();

//        kafkaTemplate.send(msg);
        kafkaTemplate.send(withTypeHeader(topics.getLike(), event, LikeEvent.LikeCountIncreased.class));

        return LikeResult.LikeRegisterResult.newCreated(userId, productId);
    }


    private <T> ProducerRecord<String, Object> withTypeHeader(String topic, T value, Class<?> type) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, value);
        record.headers().remove(TYPE_ID_HEADER); // 혹시 기존 값이 있으면 제거
        record.headers().add(
                TYPE_ID_HEADER,
                type.getName().getBytes(StandardCharsets.UTF_8)
        );
        return record;
    }

    @Transactional
    public LikeResult.LikeRemoveResult remove(final Long userId, final Long productId) {
        // 상품 유효성 검사
        Product product = productService.retrieveOne(productId)
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "해당 상품에 좋아요를 해제 할 수 없습니다."));

        // 좋아요 해제
        LikeQuery.LikeRemoveQuery query = likeService.remove(userId, productId);
        if (query.isDuplicatedRequest()) {
            return LikeResult.LikeRemoveResult.duplicated(userId, productId);
        }

        // 좋아요 수 증가(비동기)
        LikeEvent.LikeCountDecreased event = new LikeEvent.LikeCountDecreased(productId);
        eventPublisher.publishEvent(event);
        //kafka
        kafkaTemplate.send(topics.getLike(), event);

        return LikeResult.LikeRemoveResult.newProcess(userId, productId);
    }

    public LikeResult.LikeListResult retrieveLikedProducts(
            final Long userId,
            final Optional<PagingCondition> pagingCondition
    ) {
        Pageable pageable = PageableFactory.from(
                Optional.of(LikeSortType.DEFAULT),
                pagingCondition,
                LikeSortType.DEFAULT,
                PagingPolicy.LIKE.getDefaultPageSize()
        );

        //좋아요 기록 조회
        Page<LikeHistory> likeHistories = likeService.retrieveHistories(userId, pageable);

        if (likeHistories.isEmpty()) {
            return new LikeResult.LikeListResult(
                    Collections.emptyList(),
                    new Pagination(0L, pageable.getPageNumber(), pageable.getPageSize())
            );
        }

        // 상품 ID로 상품 정보 조회
        List<Long> productIds = likeHistories.getContent().stream()
                .map(LikeHistory::getProductId)
                .distinct()
                .toList();

        List<Product> products = productService.getProducts(productIds);

        List<Long> brandIds = products.stream()
                .map(Product::getBrandId)
                .distinct()
                .toList();

        List<Brand> brands = brandService.getBrandsOfProducts(brandIds);

        List<LikeResult.LikeDetailResult> likeDetailResults =
                likeProductService.assembleLikeProductInfo(likeHistories.getContent(), products, brands);

        return new LikeResult.LikeListResult(
                likeDetailResults,
                new Pagination(
                        likeHistories.getTotalElements(),
                        pageable.getPageNumber(),
                        pageable.getPageSize()
                )
        );
    }
}
