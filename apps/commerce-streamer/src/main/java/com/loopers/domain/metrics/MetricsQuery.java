package com.loopers.domain.metrics;

import lombok.AccessLevel;
import lombok.Builder;

public class MetricsQuery {
    @Builder(access = AccessLevel.PRIVATE)
    public record Save(
            String messageId,
            boolean success,
            String errorMessage
    ) {
        public static Save created(String messageId) {
            return Save.builder()
                    .messageId(messageId)
                    .success(true)
                    .errorMessage(null)
                    .build();
        }

        public static Save failed(String messageId, String errorMessage) {
            return Save.builder()
                    .messageId(messageId)
                    .success(false)
                    .errorMessage(errorMessage)
                    .build();
        }
    }
}
