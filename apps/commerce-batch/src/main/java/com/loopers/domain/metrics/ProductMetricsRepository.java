package com.loopers.domain.metrics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ProductMetricsRepository {
    Page<ProductMetrics> findByMetricsDateBetween(LocalDate from, LocalDate to, Pageable pageable);
}
