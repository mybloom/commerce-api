package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class LikeService {

    private final LikeHistoryRepository likeHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LikeHistory register(Long userId, Long productId) {
        LikeHistory likeHistory;
        try {
            likeHistory = likeHistoryRepository.save(LikeHistory.from(userId, productId));
        }catch (DataIntegrityViolationException e){
            throw new CoreException(ErrorType.CONFLICT,"이미 좋아요를 등록한 상품입니다.");
        }
        return likeHistory;
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
