package com.loopers.interfaces.consumer;

import com.loopers.domain.metrics.ProductMetricsService;
import com.loopers.domain.sharedkernel.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static java.lang.Boolean.FALSE;

@Slf4j
@RequiredArgsConstructor
@Component
@KafkaListener(
        topics = "#{@kafkaTopicsProperties.likeEvent}",
        groupId = "like-event-metrics-consumers"
)
public class LikeEventMetricsConsumer {
    private static final String IDEMPOTENCY_KEY_PREFIX = "like-event-metrics-consumers:";
    private static final long MESSAGE_TTL_SECONDS = 60 * 60; // 1시간 TTL
    private static final String FINISHED_PROCESSING = "processed";

    private final ProductMetricsService productMetricsService;
    private final StringRedisTemplate redisTemplate;

    @KafkaHandler
    public void handleLikeIncreased(
            LikeEvent.LikeCountIncreased payload,
            @Header("messageId") String messageId,
            @Header("publishedAt") String publishedAt,
            @Header("version") String version,
            Acknowledgment acknowledgment
    ) {
        log.info("msgId={}, publishedAt={}, payload={}", messageId, publishedAt, payload);
        try {
            // Idempotency 처리: Redis에 메시지 ID로 키를 저장하고, 이미 존재하면 중복 처리로 간주
            String redisKey = IDEMPOTENCY_KEY_PREFIX + messageId;
            Boolean firstProcess = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, FINISHED_PROCESSING, Duration.ofSeconds(MESSAGE_TTL_SECONDS));

            if (FALSE.equals(firstProcess)) {
                log.warn("Duplicate message detected. Skipping. messageId={}", messageId);
                acknowledgment.acknowledge(); // 중복이지만 commit은 해줌
                return;
            }

            // 비즈니스 로직 처리
            productMetricsService.increaseLikeCount(payload);

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
            Acknowledgment acknowledgment
    ) {
        log.info("msgId={}, publishedAt={}, payload={}", messageId, publishedAt, payload);
        try {
            // Idempotency 처리: Redis에 메시지 ID로 키를 저장하고, 이미 존재하면 중복 처리로 간주
            String redisKey = IDEMPOTENCY_KEY_PREFIX + messageId;
            Boolean firstProcess = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, FINISHED_PROCESSING, Duration.ofSeconds(MESSAGE_TTL_SECONDS));

            if (FALSE.equals(firstProcess)) {
                log.warn("Duplicate message detected. Skipping. messageId={}", messageId);
                acknowledgment.acknowledge(); // 중복이지만 commit은 해줌
                return;
            }

            // 비즈니스 로직 처리
            productMetricsService.decreaseLikeCount(payload);

            // 수동 커밋
            acknowledgment.acknowledge();
        } catch (
                Exception e) {
            log.error("Error processing LikeCountDecreased: {}", e.getMessage(), e);
            // ErrorHandler가 처리하도록 예외 재발생 //todo: TEST필요, DLQ
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
