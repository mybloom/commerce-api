package com.loopers.domain.like;


import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LikeHistoryRepository {
    LikeHistory save(LikeHistory likeHistory);

    Optional<LikeHistory> findById(Long id);

    Optional<LikeHistory> findByUserIdAndProductId(Long userId, Long productId);

    Page<LikeHistory> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);
}
