package com.loopers.domain.like;

import com.loopers.domain.sharedkernel.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class LikeService {

    private final LikeHistoryRepository likeHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LikeQuery.LikeRegisterQuery register(Long userId, Long productId) {
        final Optional<LikeHistory> optional = likeHistoryRepository.findByUserIdAndProductId(userId, productId);

        if (optional.isPresent()) {
            log.info("이미 좋아요가 등록되어 있습니다. userId: {}, productId: {}", userId, productId);
            return LikeQuery.LikeRegisterQuery.alreadyRegister(optional.get());
        }

        final LikeHistory saved = likeHistoryRepository.save(LikeHistory.from(userId, productId));

        // 좋아요 수 증가(비동기)
        LikeEvent.LikeCountIncreased event = new LikeEvent.LikeCountIncreased(productId);
        eventPublisher.publishEvent(event);

        return LikeQuery.LikeRegisterQuery.success(saved);
    }

    @Transactional
    public LikeQuery.LikeRemoveQuery remove(final Long userId, final Long productId) {
        int deletedCount = likeHistoryRepository.deleteByUserIdAndProductId(userId, productId);

        if (deletedCount == 0) {
            log.info("좋아요가 이미 해제되어 있습니다. userId: {}, productId: {}", userId, productId);
            return LikeQuery.LikeRemoveQuery.alreadyRemoved(userId, productId);
        }

        return LikeQuery.LikeRemoveQuery.success(userId, productId);
    }

    public Page<LikeHistory> retrieveHistories(final Long userId, final Pageable pageable) {
        return likeHistoryRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
    }
}
