package com.loopers.domain.product;

import com.loopers.config.kafka.KafkaTopicsProperties;
import com.loopers.domain.sharedkernel.KafkaMessage;
import com.loopers.domain.sharedkernel.KafkaRecordFactory;
import com.loopers.domain.sharedkernel.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;

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
    public void onLikeCountIncrease(LikeEvent.LikeCountIncreased  event) {
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
    public void handleLiDecrease(LikeEvent.LikeCountDecreased event) {
        log.info("좋아요가 해제 되었습니다.productId: {}", event.productId());
        productService.decreaseLikeCount(event.productId());
    }

    private <T> ProducerRecord<String, Object> withTypeHeader(
            String topic,
            String key,
            T value,
            Class<?> type
    ) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, value);

        // 혹시 기본 __TypeId__ 헤더가 있으면 제거 후 다시 세팅
        record.headers().remove(TYPE_ID_HEADER);
        record.headers().add(
                TYPE_ID_HEADER,
                type.getName().getBytes(StandardCharsets.UTF_8)
        );

        return record;
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
}
