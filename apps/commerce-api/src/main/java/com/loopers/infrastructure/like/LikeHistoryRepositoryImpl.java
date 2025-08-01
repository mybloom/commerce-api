package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeHistory;
import com.loopers.domain.like.LikeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class LikeHistoryRepositoryImpl implements LikeHistoryRepository {

    private final LikeHistoryJpaRepository likeHistoryJpaRepository;

    @Override
    public LikeHistory save(LikeHistory likeHistory) {
        return likeHistoryJpaRepository.save(likeHistory);
    }

    @Override
    public Optional<LikeHistory> findById(Long id) {
        return likeHistoryJpaRepository.findById(id);
    }

    @Override
    public Optional<LikeHistory> findByUserIdAndProductId(Long userId, Long productId) {
        return likeHistoryJpaRepository.findByUserIdAndProductId(userId, productId);
    }
}
