package com.loopers.application.like;

import com.loopers.application.common.PagingCondition;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeHistory;
import com.loopers.domain.like.LikeQuery;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeSortType;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.support.paging.PageableFactory;
import com.loopers.support.paging.Pagination;
import com.loopers.support.paging.PagingPolicy;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LikeUseCase {
    private final LikeService likeService;
    private final ProductService productService;
    private final BrandService brandService;

    @Transactional
    public LikeResult.LikeRegisterResult register(final Long userId, final Long productId){
        //상품 유효성 검사
        Product product = productService.retrieveOne(productId)
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "해당 상품에 좋아요를 할 수 없습니다."));

        //좋아요 등록
        final LikeQuery.LikeRegisterQuery likeRegisterQuery = likeService.register(userId, productId);

        //좋아요 수 증가
        if (!likeRegisterQuery.isDuplicatedRequest()) {
            productService.increaseLikeCount(product);
        }

        return LikeResult.LikeRegisterResult.from(likeRegisterQuery);
    }

    @Transactional
    public LikeResult.LikeRemoveResult remove(final Long userId, final Long productId){
        //상품 유효성 검사
        Product product = productService.retrieveOne(productId)
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "해당 상품에 좋아요를 해제 할 수 없습니다."));

        //좋아요 해제
        LikeQuery.LikeRemoveQuery likeRemoveQuery = likeService.remove(userId, productId);

        //좋아요 수 감소
        if (!likeRemoveQuery.isDuplicatedRequest()) {
            productService.decreaseLikeCount(product);
        }

        return LikeResult.LikeRemoveResult.from(likeRemoveQuery);
    }


}
