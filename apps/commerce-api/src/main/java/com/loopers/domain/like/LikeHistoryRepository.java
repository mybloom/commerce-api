package com.loopers.domain.like;

import io.micrometer.observation.ObservationFilter;

import java.util.Optional;

public interface LikeHistoryRepository {
    LikeHistory save(LikeHistory likeHistory);

    Optional<LikeHistory> findById(Long id);

    Optional<LikeHistory> findByUserIdAndProductId(Long userId, Long productId);
}
