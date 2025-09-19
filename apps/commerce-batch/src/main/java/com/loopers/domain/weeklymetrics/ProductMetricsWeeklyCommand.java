package com.loopers.domain.weeklymetrics;

import java.time.LocalDate;

public class ProductMetricsWeeklyCommand {
    public record Create(
            Long productId,
            Long viewCount,
            Long likeCount,
            Long purchaseCount,
            LocalDate windowStart,
            LocalDate windowEnd
    ) {
    }
}
