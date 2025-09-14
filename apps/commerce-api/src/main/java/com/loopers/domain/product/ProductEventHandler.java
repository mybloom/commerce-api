package com.loopers.domain.product;

import com.loopers.config.kafka.KafkaTopicsProperties;
import com.loopers.domain.sharedkernel.KafkaRecordFactory;
import com.loopers.domain.sharedkernel.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Slf4j
@RequiredArgsConstructor
@Component
public class ProductEventHandler {

    private final ProductService productService;

    private static final String TYPE_ID_HEADER = "__TypeId__";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeCountIncrease(LikeEvent.LikeCountIncreased event) {
        log.info("좋아요가 새로 등록되었습니다.productId: {}", event.productId());
        productService.increaseLikeCountAtomically(event.productId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLikeCountIncrease(LikeEvent.LikeCountIncreased event) {
        // 파티션 키는 productId 기준으로 (순서 보장 목적)
        String partitionKey = String.valueOf(event.productId());
        kafkaTemplate.send(
                KafkaRecordFactory.withTypeHeader(
                        topics.getLikeEvent(),
                        partitionKey,
                        event
                )
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeDecrease(LikeEvent.LikeCountDecreased event) {
        log.info("좋아요가 해제 되었습니다.productId: {}", event.productId());
        productService.decreaseLikeCount(event.productId());
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLikeDecrease(LikeEvent.LikeCountDecreased event) {
        // 파티션 키는 productId 기준으로 (순서 보장 목적)
        String partitionKey = String.valueOf(event.productId());
        kafkaTemplate.send(
                KafkaRecordFactory.withTypeHeader(
                        topics.getLikeEvent(),
                        partitionKey,
                        event
                )
        );
    }
}
