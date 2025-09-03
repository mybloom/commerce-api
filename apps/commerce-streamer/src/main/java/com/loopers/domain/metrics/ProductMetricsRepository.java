package com.loopers.domain.metrics;

import java.util.Optional;

public interface ProductMetricsRepository {
    int increaseLikeCountAtomically(Long productId);

    Optional<ProductMetrics> findByProductId(Long productId);

    ProductMetrics save(ProductMetrics productMetrics);
}
