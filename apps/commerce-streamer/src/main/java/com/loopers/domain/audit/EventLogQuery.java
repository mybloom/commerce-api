package com.loopers.domain.audit;

public class EventLogQuery {
    public record Save(
            String messageId,
            boolean isDuplicated
    ) {
        public static Save duplicate(EventLog messageId) {
            return new Save(messageId.getMessageId(), true);
        }

        public static Save created(String messageId) {
            return new Save(messageId, false);
        }
    }
}
