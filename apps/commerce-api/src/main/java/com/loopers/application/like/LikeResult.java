package com.loopers.application.like;

import com.loopers.domain.like.LikeQuery;

public class LikeResult {
    public record LikeRegisterResult(
            Long userId,
            Long productId,
            boolean isDuplicatedRequest
    ) {
        public static LikeRegisterResult from(LikeQuery.LikeRegisterQuery likeRegisterQuery) {
            return new LikeRegisterResult(
                    likeRegisterQuery.likeHistory().getUserId(),
                    likeRegisterQuery.likeHistory().getProductId(),
                    likeRegisterQuery.isDuplicatedRequest()
            );
        }
    }

    public record LikeRemoveResult(
            Long userId,
            Long productId,
            boolean isDuplicatedRequest
    ) {
        public static LikeRemoveResult from(LikeQuery.LikeRemoveQuery likeRemoveQuery) {
            return new LikeRemoveResult(
                    likeRemoveQuery.userId(),
                    likeRemoveQuery.productId(),
                    likeRemoveQuery.isDuplicatedRequest()
            );
        }
    }

    public record LikeListResult(
            List<LikeDetailResult> contents,
            int page,
            int size,
            long totalElements
    ) {
        public static LikeListResult of(Page<LikeDetailResult> page) {
            return new LikeListResult(
                    page.getContent(),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements()
            );
        }
    }

    public record LikeDetailResult(
            Long productId,
            String productName,
            Long price,
            String brandName
    ) {
        public static LikeDetailResult from(Product product, Brand brand) {
            return new LikeDetailResult(
                    product.getId(),
                    product.getName(),
                    product.getPrice().getAmount(),
                    brand.getName()
            );
        }
    }
}
