package com.loopers.domain.audit;

import java.time.LocalDateTime;

public class EventLogCommand {
    public record CreateEventLog(
            String messageId,
            String topic,
            String eventType,
            Integer partitionNo,
            Long offsetNo,
            String handler,
            String payload,
            String keyValue,
            LocalDateTime publishedAt
    ) {
    }
}
