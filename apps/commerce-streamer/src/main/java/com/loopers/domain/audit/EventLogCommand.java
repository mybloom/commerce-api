package com.loopers.domain.audit;

import java.time.LocalDateTime;

public class EventLogCommand {
    public record CreateEventLog(
            String messageId,
            String topic,
            String eventType,
            String payload,
            LocalDateTime publishedAt
    ) {
    }
}
