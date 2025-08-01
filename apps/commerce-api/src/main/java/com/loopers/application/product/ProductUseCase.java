package com.loopers.application.product;

import com.loopers.support.paging.PageableFactory;
import com.loopers.application.common.PagingCondition;
import com.loopers.application.product.ProductQueryResult.ListViewResult;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductListProjection;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductSortType;
import com.loopers.support.paging.Pagination;
import com.loopers.support.paging.PagingPolicy;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductUseCase {

    private final BrandService brandService;
    private final ProductService productService;

    public ProductQueryResult.ListViewResult findList(
        final Optional<Long> brandId,
        final Optional<ProductSortType> productSortType,
        final Optional<PagingCondition> pagingCondition
    ) {
        Pageable pageable = PageableFactory.from(
            productSortType,
            pagingCondition,
            ProductSortType.DEFAULT,
            PagingPolicy.PRODUCT.getDefaultPageSize()
        );

        return brandId
            .map(it -> retrieveListByBrand(it, pageable))
            .orElseGet(() -> retrieveList(pageable));
    }

    private ListViewResult retrieveListByBrand(final Long brandId, final Pageable pageable) {
        final Optional<Brand> brand = brandService.retrieveById(brandId);

        if (brand.isEmpty()) {
            log.error("Brand with ID {} does not exist", brandId);

            return ListViewResult.from(
                Collections.emptyList(),
                new Pagination(0, pageable.getPageNumber(), pageable.getPageSize())
            );
        }

        final Page<ProductListProjection> productPage = productService.retrieveListByBrand(brandId, pageable);

        return ListViewResult.from(
            productPage.getContent(),
            new Pagination(productPage.getTotalElements(), pageable.getPageNumber(), pageable.getPageSize())
        );
    }

    private ListViewResult retrieveList(final Pageable pageable) {
        final Page<ProductListProjection> productPage = productService.retrieveList(pageable);

        return ListViewResult.from(
            productPage.getContent(),
            new Pagination(productPage.getTotalElements(), pageable.getPageNumber(), pageable.getPageSize())
        );
    }
}
