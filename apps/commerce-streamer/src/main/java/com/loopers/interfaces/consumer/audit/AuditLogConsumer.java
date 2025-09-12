package com.loopers.interfaces.consumer.audit;

import com.loopers.application.audit.AuditUseCase;
import com.loopers.domain.audit.AuditLogCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuditLogConsumer {

    private final AuditUseCase auditLogUseCase;

    /**
     * like-event, order-event 등 여러 토픽을 하나의 그룹에서 처리
     * 운영 후 이벤트가 많이 발행되는 이벤트만 분리해도 될 것 같다고 판단.
     */
    @KafkaListener(
            topics = {
                    "#{@kafkaTopicsProperties.likeEvent}",
                    "#{@kafkaTopicsProperties.orderEvent}",
                    "#{@kafkaTopicsProperties.paymentEvent}"
            },
            groupId = "audit-consumer"
    )
    public void consumeAuditLogs(
            ConsumerRecord<String, Object> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header("__TypeId__") String eventType,
            @Header("messageId") String messageId,
            @Header("publishedAt") String publishedAt,
            @Header(value = "version", required = false) String version,
            Acknowledgment acknowledgment
    ) {
        final String handler = "audit-consumer"; // groupId와 동일하게 지정

        try {
            log.info("AUDIT | topic={}, msgId={}, publishedAt={}, version={}, handler={}, payload={}",
                    topic, messageId, publishedAt, version, handler, record.value().toString());

            AuditLogCommand.Create command = new AuditLogCommand.Create(
                    messageId,
                    topic,
                    eventType,
                    record.partition(),
                    record.offset(),
                    handler,
                    record.value().toString(),
                    record.key() != null ? record.key().toString() : null,
                    LocalDateTime.now() // publishedAt 대체 (헤더 값 파싱 가능 시 교체)
            );
            auditLogUseCase.save(command);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error saving audit log. topic={}, msgId={}, handler={}, error={}",
                    topic, messageId, handler, e.getMessage(), e);
            throw e; // DLQ로 보낼 수 있도록 예외 재발생
        }
    }

}
