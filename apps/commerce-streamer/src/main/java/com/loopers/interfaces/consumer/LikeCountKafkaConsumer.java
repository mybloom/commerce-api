package com.loopers.interfaces.consumer;

import com.loopers.config.kafka.KafkaTopicsProperties;
import com.loopers.domain.metrics.LikeEvent;
import com.loopers.domain.metrics.ProductMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LikeCountKafkaConsumer {
    private final KafkaTopicsProperties topics;
    private final ProductMetricsService productMetricsService;

    //    @KafkaListener(topics = "${commerce-kafka.topics.like}")
    @KafkaListener(topics = "#{@kafkaTopicsProperties.like}") //todo:KafkaTopicsProperties SpEL로 가능
    public void likeCountListener(
            @Payload LikeEvent.LikeCountIncreased event,
            ConsumerRecord<String, Object> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("***message:{}", messages);
        productMetricsService.incrementLikeCount(event);
        acknowledgment.acknowledge(); // manual ack
    }
}
