package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeHistoryJpaRepository extends JpaRepository<LikeHistory, Long> {
    Optional<LikeHistory> findByUserIdAndProductId(Long userId, Long productId);
}
