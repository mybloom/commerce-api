package com.loopers.domain.sharedkernel;

import java.time.LocalDate;

public class ProductMetricsEvent {

    public record Updated(
            Long productMetricsId,
            Long productId,
            LocalDate metricsDate
    ) {
    }
}
