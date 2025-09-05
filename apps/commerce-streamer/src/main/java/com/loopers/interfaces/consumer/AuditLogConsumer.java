package com.loopers.interfaces.consumer;

import com.loopers.domain.audit.EventLogCommand;
import com.loopers.domain.audit.EventLogQuery;
import com.loopers.domain.audit.EventLogService;
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
    private final EventLogService eventLogService; // 실제 DB에 저장하는 서비스

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
        try {
            log.info("AUDIT | topic={}, msgId={}, publishedAt={}, version={}, payload={}",
                    topic, messageId, publishedAt, version, record.value().toString());

            // DB에 저장 (토픽별로 구분해서 저장 가능)
            EventLogQuery.Save query = eventLogService.saveLog(
                    new EventLogCommand.CreateEventLog(
                            messageId,
                            topic,
                            eventType,
                            record.value().toString(),
                            LocalDateTime.now() // publishedAt,
                    )
            );

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error saving audit log. topic={}, msgId={}, error={}",
                    topic, messageId, e.getMessage(), e);
            throw e; // DLQ로 보낼 수 있도록 예외 재발생
        }
    }
}
