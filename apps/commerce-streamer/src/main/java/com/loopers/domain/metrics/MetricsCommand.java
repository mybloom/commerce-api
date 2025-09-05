package com.loopers.domain.metrics;

import java.util.List;

public class MetricsCommand {

    public record Create(
            String messageId,
            String handler,
            Long productId,
            MetricsEventType metricsEventType
    ) {
    }

    public record CreatePurchase(
            String messageId,
            String handler,
            List<Product> products,
            MetricsEventType metricsEventType
    ) {
        public record Product(
                Long productId,
                int quantity
        ) {
        }
    }
}
