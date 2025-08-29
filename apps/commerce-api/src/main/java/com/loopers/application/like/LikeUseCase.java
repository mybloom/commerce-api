package com.loopers.application.like;

import com.loopers.application.common.PagingCondition;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.*;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.support.paging.PageableFactory;
import com.loopers.support.paging.Pagination;
import com.loopers.support.paging.PagingPolicy;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

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
}
