package com.loopers.domain.like;


public class LikeQuery {
    public record LikeRegisterQuery(
            LikeHistory likeHistory,
            boolean isDuplicatedRequest
    ) {
        public static LikeRegisterQuery success(LikeHistory likeHistory) {
            return new LikeRegisterQuery(likeHistory, false);
        }

        public static LikeRegisterQuery alreadyRegister(LikeHistory likeHistory) {
            return new LikeRegisterQuery(likeHistory, true);
        }
    }

    public record LikeRemoveQuery(
            boolean isDuplicatedRequest,
            Long userId,
            Long productId
    ) {
        public static LikeRemoveQuery success(Long userId, Long productId) {
            return new LikeRemoveQuery(false, userId, productId);
        }

        public static LikeRemoveQuery alreadyRemoved(Long userId, Long productId) {
            return new LikeRemoveQuery(true, userId, productId);
        }
    }
}
