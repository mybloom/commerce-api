package com.loopers.domain.audit;

import java.time.LocalDateTime;

public class AuditLogCommand {
    public record Create(
            String messageId, // UUID
            String topic,
            String eventType,
            Integer partitionNo,
            Long offsetNo,
            String handler, // consumer group id
            String payload,
            String keyValue,
            LocalDateTime publishedAt
    ) {
    }
}
