package com.loopers.interfaces.consumer;

import com.loopers.config.kafka.KafkaTopicsProperties;
import com.loopers.domain.metrics.ProductMetricsService;
import com.loopers.domain.sharedkernel.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@KafkaListener(topics = "#{@kafkaTopicsProperties.like}")
public class LikeCountKafkaConsumer {
    private final KafkaTopicsProperties topics;
    private final ProductMetricsService productMetricsService;


    @KafkaHandler
    public void likeCountListener(
            LikeEvent.LikeCountIncreased event,
            ConsumerRecord<String, Object> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("***message:{}", messages);
        productMetricsService.increaseLikeCount(event);
        acknowledgment.acknowledge(); // manual ack
    }
}
