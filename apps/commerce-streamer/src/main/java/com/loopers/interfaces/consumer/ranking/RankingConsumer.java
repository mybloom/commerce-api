package com.loopers.interfaces.consumer.ranking;

import com.loopers.application.ranking.RankingInfo;
import com.loopers.application.ranking.RankingUseCase;
import com.loopers.config.kafka.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class RankingConsumer {
    private static final String HANDLER = "ranking-consumers";
    private final RankingUseCase rankingUseCase;

    @KafkaListener(
            topics = {
                    "#{@kafkaTopicsProperties.productMetricsEvent}"
            },
            groupId = "ranking-consumers",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleProductMetricsLikeCount(
            List<ConsumerRecord<String, Object>> messages,
            Acknowledgment acknowledgment
    ) {
        List<RankingInfo.Create> info = messages.stream()
                .map(record -> RankingRequest.Create.from(record, HANDLER).convertToInfo())
                .toList();
        rankingUseCase.rank(info);

        acknowledgment.acknowledge();
    }
}
