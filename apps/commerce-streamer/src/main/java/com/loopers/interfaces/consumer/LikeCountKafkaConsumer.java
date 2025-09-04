package com.loopers.interfaces.consumer;

import com.loopers.config.kafka.KafkaTopicsProperties;
import com.loopers.domain.metrics.ProductMetricsService;
import com.loopers.domain.sharedkernel.KafkaMessage;
import com.loopers.domain.sharedkernel.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@KafkaListener(topics = "#{@kafkaTopicsProperties.likeEvent}")
public class LikeCountKafkaConsumer {
    private final KafkaTopicsProperties topics;
    private final ProductMetricsService productMetricsService;


    @KafkaHandler
    public void likeCountListener(
//            LikeEvent.LikeCountIncreased event,
            KafkaMessage<LikeEvent.LikeCountIncreased> event,
            ConsumerRecord<String, Object> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("***message:{}", messages);

        LikeEvent.LikeCountIncreased payload = event.payload();
        productMetricsService.increaseLikeCount(event.payload());
        acknowledgment.acknowledge(); // manual ack
    }

    /*@KafkaHandler
    public void handleLikeIncreased(
            @Payload LikeEventMessage.LikeIncreasedMessage event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received LikeCountIncreased: eventId={}, userId={}, productId={}, count={}, " +
                            "topic={}, partition={}, offset={}",
                    event.getEventId(), event.getUserId(), event.getProductId(),
                    event.getCurrentCount(), topic, partition, offset);

            // 비즈니스 로직 처리
//            processLikeIncreasedEvent(event);

            // 수동 커밋
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing LikeCountIncreased: {}", e.getMessage(), e);
            // ErrorHandler가 처리하도록 예외 재발생
            throw e;
        }
    }


    @KafkaHandler
    public void handleLikeDecreased(
            @Payload LikeEvent.LikeCountDecreased event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received LikeCountDecreased: eventId={}, userId={}, productId={}, count={}, " +
                            "topic={}, partition={}, offset={}",
                    event.getEventId(), event.getUserId(), event.getProductId(),
                    event.getCurrentCount(), topic, partition, offset);

            // 비즈니스 로직 처리
            processLikeDecreasedEvent(event);

            // 수동 커밋
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing LikeCountDecreased: {}", e.getMessage(), e);
            throw e;
        }
    }*/

    // 타입이 매칭되지 않는 경우를 처리하는 기본 핸들러
    @KafkaHandler(isDefault = true)
    public void handleUnknown(
            Object event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        log.warn("Received unknown event type: {}, topic={}",
                event.getClass().getSimpleName(), topic);

        // 알 수 없는 타입도 커밋하여 offset 진행
        acknowledgment.acknowledge();
    }
}
