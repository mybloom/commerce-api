package com.loopers.application.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.like.LikeQuery;
import com.loopers.domain.product.Product;
import com.loopers.support.paging.Pagination;
import java.util.List;
import org.springframework.data.domain.Page;

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


}
