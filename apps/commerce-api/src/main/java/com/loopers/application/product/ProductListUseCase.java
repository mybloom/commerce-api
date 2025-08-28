package com.loopers.application.product;

import com.loopers.support.cache.CacheTemplate;
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
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductListUseCase {
    //todo : @ConfigurationProperties(prefix = "cache.product-list") 방식으로 수정
    private static final List<Integer> CACHEABLE_PAGES = List.of(0, 1);

    private final BrandService brandService;
    private final ProductService productService;
    private final CacheTemplate cache;
    private final ProductListCachePolicy cachePolicy;

    public ListViewResult findList(
            final Optional<Long> userId,
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

        if (!isCachePage(pageable)) {
            final Page<ProductListProjection> productPage = productService.retrieveListByBrand(brandId, pageable);
            return ListViewResult.from(
                    productPage.getContent(),
                    new Pagination(productPage.getTotalElements(), pageable.getPageNumber(), pageable.getPageSize())
            );
        }

        return getListByBrandViaCache(brandId, pageable);
    }

    private ListViewResult retrieveList(final Pageable pageable) {
        if (!isCachePage(pageable)) {
            final Page<ProductListProjection> productPage = productService.retrieveList(pageable);
            return ListViewResult.from(
                    productPage.getContent(),
                    new Pagination(productPage.getTotalElements(), pageable.getPageNumber(), pageable.getPageSize())
            );
        }

        return getListByViaCache(pageable);
    }

    private ListViewResult getListByBrandViaCache(Long brandId, Pageable pageable) {
        String key = cachePolicy.getKeyFormat()
                .formatted(brandId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().toString());
        PagePayload payload = cache.getOrLoad(
                cachePolicy.getCacheName(),
                key,
                PagePayload.class,
                () -> PagePayload.from(productService.retrieveListByBrand(brandId, pageable)), //loadFromDB
                cachePolicy.getTtl(),
                cachePolicy.getNullTtl()
        );

        return (payload == null) ?
                ListViewResult.from(
                        Collections.emptyList(),
                        new Pagination(0, pageable.getPageNumber(), pageable.getPageSize())
                ) :
                ListViewResult.from(
                        payload.items(),
                        new Pagination(payload.total(), pageable.getPageNumber(), pageable.getPageSize())
                );
    }

    private ListViewResult getListByViaCache(Pageable pageable) {
        String brandKeyPart = "all";
        String key = cachePolicy.getKeyFormat()
                .formatted(brandKeyPart, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().toString());
        PagePayload payload = cache.getOrLoad(
                cachePolicy.getCacheName(),
                key,
                PagePayload.class,
                () -> PagePayload.from(productService.retrieveList(pageable)), //loadFromDB
                cachePolicy.getTtl(),
                cachePolicy.getNullTtl()
        );

        return (payload == null) ?
                ListViewResult.from(
                        Collections.emptyList(),
                        new Pagination(0, pageable.getPageNumber(), pageable.getPageSize())
                ) :
                ListViewResult.from(
                        payload.items(),
                        new Pagination(payload.total(), pageable.getPageNumber(), pageable.getPageSize())
                );
    }

    private boolean isCachePage(Pageable pageable) {
        return CACHEABLE_PAGES.contains(pageable.getPageNumber());
    }

    private static record PagePayload(List<ProductListProjection> items, long total) {
        static PagePayload from(Page<ProductListProjection> page) {
            if (page.isEmpty()) return null; //일반 ttl과 다르게 null_ttl(짧은ttl)을 설정하기 위해 null로 반환.
            return new PagePayload(page.getContent(), page.getTotalElements());
        }
    }
}
