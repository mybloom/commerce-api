package com.loopers.domain.ranking;

import java.time.LocalDate;

public class RankingCommand {

    public record  ProductMetrics(
            Long productMetricsId,
            Long productId,
            LocalDate metricsDate,
            Long likeCount,
            Long purchaseCount,
            Long viewCount
    ) {
    }
}
