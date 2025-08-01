package com.loopers.domain.like;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeService {

    private final LikeHistoryRepository likeHistoryRepository;

    public LikeQuery.LikeRegisterQuery register(final Long userId, final Long productId) {
        final LikeHistory history = likeHistoryRepository.findByUserIdAndProductId(userId, productId)
                .orElse(null);

        if (history == null) {
            final LikeHistory saved = likeHistoryRepository.save(LikeHistory.from(userId, productId));
            return LikeQuery.LikeRegisterQuery.success(saved);
        }

        if (!history.isDeleted()) {
            return LikeQuery.LikeRegisterQuery.alreadyRegister(history);
        }

        history.restore();
        likeHistoryRepository.save(history);
        return LikeQuery.LikeRegisterQuery.success(history);
    }

    public LikeQuery.LikeRemoveQuery remove(final Long userId, final Long productId) {
        final LikeHistory existing = likeHistoryRepository.findByUserIdAndProductId(userId, productId)
                .orElse(null);

        if (existing == null) {
            return LikeQuery.LikeRemoveQuery.alreadyRemoved(userId, productId);
        }

        if(existing.isDeleted()){
            return LikeQuery.LikeRemoveQuery.alreadyRemoved(userId,productId);
        }

        existing.delete();
        likeHistoryRepository.save(existing);

        return LikeQuery.LikeRemoveQuery.success(userId, productId);
    }

    public Page<LikeHistory> retrieveHistories(final Long userId, final Pageable pageable) {
        return likeHistoryRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
    }
}
