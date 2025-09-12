package com.loopers.domain.ranking;

import com.loopers.config.kafka.KafkaTopicsProperties;
import com.loopers.domain.sharedkernel.KafkaRecordFactory;
import com.loopers.domain.sharedkernel.ProductMetricsEvent;
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
public class RankingEventHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductMetricsUpdated(ProductMetricsEvent.Updated event) {
        // 파티션 키는 productMetricsId 기준으로 (순서 보장 목적-> 여기서는 최신것만 처리되도록 zip(distincs)하는 목적)
        String partitionKey = String.valueOf(event.productMetricsId());
        kafkaTemplate.send(
                KafkaRecordFactory.withTypeHeader(
                        topics.getProductMetricsEvent(),
                        partitionKey,
                        event
                )
        );
    }
}
