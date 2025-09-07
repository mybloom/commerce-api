package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.metrics.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ProductMetricsRepositoryImpl implements ProductMetricsRepository {
    private final ProductMetricsJpaRepository productMetricsJpaRepository;

    @Override
    public int increaseLikeCountAtomically(Long productId) {
        return productMetricsJpaRepository.increaseLikeCountAtomically(productId);
    }

    public int decreaseLikeCountAtomically(Long productId) {
        return productMetricsJpaRepository.decreaseLikeCountAtomically(productId);
    }

    @Override
    public Optional<ProductMetrics> findByProductId(Long productId) {
        return productMetricsJpaRepository.findByProductId(productId);
    }

    @Override
    public ProductMetrics save(ProductMetrics productMetrics) {
        return productMetricsJpaRepository.save(productMetrics);
    }

    @Override
    public Optional<ProductMetrics> findByProductIdAndMetricsDate(Long productId, LocalDate metricsDate) {
        return productMetricsJpaRepository.findByProductIdAndMetricsDate(productId, metricsDate);
    }
}
