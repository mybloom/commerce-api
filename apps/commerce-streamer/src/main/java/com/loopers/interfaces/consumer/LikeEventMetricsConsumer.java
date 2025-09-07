package com.loopers.interfaces.consumer;

import com.loopers.application.metrics.MetricsUseCase;
import com.loopers.domain.metrics.MetricsCommand;
import com.loopers.domain.metrics.MetricsEventType;
import com.loopers.domain.sharedkernel.LikeEvent;
import com.loopers.domain.sharedkernel.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@KafkaListener(
        topics = "#{@kafkaTopicsProperties.likeEvent}",
        groupId = "like-event-metrics-consumers"
)
public class LikeEventMetricsConsumer {
    private static final String HANDLER = "like-event-metrics-consumers";

    private final MetricsUseCase metricsUseCase;
    private final StringRedisTemplate redisTemplate;

    @KafkaHandler
    public void handleLikeIncreased(
            LikeEvent.LikeCountIncreased payload,
            @Header("messageId") String messageId,
            @Header("publishedAt") String publishedAt,
            @Header("version") String version,
            @Header("__TypeId__") String typeId,
            Acknowledgment acknowledgment
    ) {
        log.info("msgId={}, publishedAt={}, payload={}", messageId, publishedAt, payload);
        try {
            MetricsCommand.Create command = new MetricsCommand.Create(
                    messageId,
                    HANDLER,
                    payload.productId(),
                    MetricsEventType.LIKE_ADDED
            );

            metricsUseCase.save(command);

            // 수동 커밋
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing LikeCountIncreased: {}", e.getMessage(), e);
            // ErrorHandler가 처리하도록 예외 재발생 //todo: TEST필요, DLQ //재시도1번, DLQ보관만 => DLQ는 redis에 저장된 키가 남아있어 중복처리 방지됨
            throw e;
        }
    }

    @KafkaHandler
    public void handleLikeDecreased(
            LikeEvent.LikeCountDecreased payload,
            @Header("messageId") String messageId,
            @Header("publishedAt") String publishedAt,
            @Header("version") String version,
            @Header("__TypeId__") String typeId,
            Acknowledgment acknowledgment
    ) {
        log.info("msgId={}, publishedAt={}, payload={}", messageId, publishedAt, payload);
        try {
            MetricsCommand.Create command = new MetricsCommand.Create(
                    messageId,
                    HANDLER,
                    payload.productId(),
                    MetricsEventType.LIKE_REMOVED
            );

            metricsUseCase.save(command);

            // 수동 커밋
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing LikeCountIncreased: {}", e.getMessage(), e);
            // ErrorHandler가 처리하도록 예외 재발생 //todo: TEST필요, DLQ //재시도1번, DLQ보관만 => DLQ는 redis에 저장된 키가 남아있어 중복처리 방지됨
            throw e;
        }
    }

    @KafkaHandler
    public void handlePurchased(
            OrderEvent.OrderSucceeded payload,
            @Header("messageId") String messageId,
            @Header("publishedAt") String publishedAt,
            @Header("version") String version,
            @Header("__TypeId__") String typeId,
            Acknowledgment acknowledgment
    ) {
        log.info("msgId={}, publishedAt={}, payload={}", messageId, publishedAt, payload);
        try {
            MetricsCommand.CreatePurchase command = new MetricsCommand.CreatePurchase(
                    messageId,
                    HANDLER,
                    payload.products().stream()
                            .map(
                                    product -> new MetricsCommand.CreatePurchase.Product(product.productId(), product.quantity())
                            ).toList(),
                    MetricsEventType.PURCHASED
            );

            metricsUseCase.savePurchase(command);

            // 수동 커밋
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing LikeCountIncreased: {}", e.getMessage(), e);
            // ErrorHandler가 처리하도록 예외 재발생 //todo: TEST필요, DLQ //재시도1번, DLQ보관만 => DLQ는 redis에 저장된 키가 남아있어 중복처리 방지됨
            throw e;
        }
    }

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

