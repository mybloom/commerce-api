package com.loopers.domain.audit;

public class AuditLogQuery {
    public record Save(
            String messageId,
            boolean isDuplicated
    ) {
        public static Save duplicated(String messageId) {
            return new Save(messageId, true);
        }

        public static Save created(String messageId) {
            return new Save(messageId, false);
        }
    }
}
