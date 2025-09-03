package com.loopers.domain.metrics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class ProductMetricsService {
    private final ProductMetricsRepository productMetricsRepository;

    @Transactional
    public void incrementLikeCount(LikeEvent.LikeCountIncreased event) {
        int updatedCount = productMetricsRepository.increaseLikeCountAtomically(event.productId());
        if (updatedCount == 0) {
            throw new IllegalStateException("Failed to update like count for productId: " + event.productId());
        }
    }
}
