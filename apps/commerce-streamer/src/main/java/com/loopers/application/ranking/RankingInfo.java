package com.loopers.application.ranking;


import java.time.LocalDate;
import java.time.LocalDateTime;

public class RankingInfo {
    public record Create(
            String messageId,
            String handler,
            LocalDateTime publishedAt,
            Long productMetricsId,
            Long productId,
            LocalDate metricsDate
    ) {
    }
}
