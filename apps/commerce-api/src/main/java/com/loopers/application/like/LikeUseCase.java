package com.loopers.application.like;

import com.loopers.application.common.PagingCondition;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeHistory;
import com.loopers.domain.like.LikeProductService;
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
import java.util.List;
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
    private final LikeProductService likeProductService = new LikeProductService();

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

   public LikeResult.LikeListResult retrieveLikedProducts(
        final Long userId,
        final Optional<PagingCondition> pagingCondition
    ) {
        Pageable pageable = PageableFactory.from(
            Optional.of(LikeSortType.DEFAULT),
            pagingCondition,
            LikeSortType.DEFAULT,
            PagingPolicy.LIKE.getDefaultPageSize()
        );

        //좋아요 기록 조회
        Page<LikeHistory> likeHistories = likeService.retrieveHistories(userId, pageable);

        if (likeHistories.isEmpty()) {
            return new LikeResult.LikeListResult(
                Collections.emptyList(),
                new Pagination(0L, pageable.getPageNumber(), pageable.getPageSize())
            );
        }

        // 상품 ID로 상품 정보 조회
        List<Long> productIds = likeHistories.getContent().stream()
            .map(LikeHistory::getProductId)
            .distinct()
            .toList();

        List<Product> products = productService.getProducts(productIds);

        List<Long> brandIds = products.stream()
            .map(Product::getBrandId)
            .distinct()
            .toList();

        List<Brand> brands = brandService.getBrandsOfProducts(brandIds);

       List<LikeResult.LikeDetailResult> likeDetailResults =
           likeProductService.assembleLikeProductInfo(likeHistories.getContent(), products, brands);

        return new LikeResult.LikeListResult(
            likeDetailResults,
            new Pagination(
                likeHistories.getTotalElements(),
                pageable.getPageNumber(),
                pageable.getPageSize()
            )
        );
    }

    @Transactional
    public LikeResult.LikeRegisterResult registerV2(final Long userId, final Long productId){
        // 1. 상품 유효성 검사 (락 없이)
        Product product = productService.retrieveOne(productId)
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "해당 상품에 좋아요를 할 수 없습니다."));

        // 2. 좋아요 등록 시도 (멱등성 검사 포함)
        final LikeQuery.LikeRegisterQuery likeRegisterQuery = likeService.register(userId, productId);

        // 3. 중복이 아닐 때만 락 걸고 좋아요 수 증가
        if (!likeRegisterQuery.isDuplicatedRequest()) {
            // 상품을 비관적 락으로 다시 조회
            Product lockedProduct = productService.retrieveWithPessimisticLock(productId);
            productService.increaseLikeCount(lockedProduct);
        }

        return LikeResult.LikeRegisterResult.from(likeRegisterQuery);
    }

}
