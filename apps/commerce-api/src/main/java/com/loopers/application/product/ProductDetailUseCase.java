package com.loopers.application.product;


import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductQuery;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductDetailUseCase {

    private final BrandService brandService;
    private final ProductService productService;

    public ProductQueryResult.CatalogDetailResult findDetail(final Optional<Long> userId, final Long productId) {
        ProductQuery.ProductDetailQuery productDetailQuery = productService.retrieveOneByCache(productId);
        if(productDetailQuery == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "유효한 상품을 찾을 수 없습니다.");
        }

        final Brand brand = brandService.retrieveById(productDetailQuery.brandId())
                .orElseThrow(() -> {
                    log.error("Brand with ID {} does not exist", productDetailQuery.brandId());
                    return new CoreException(ErrorType.BAD_REQUEST, "유효한 상품을 찾을 수 없습니다.");
                });

        return ProductQueryResult.CatalogDetailResult.from(brand, productDetailQuery);
    }
}
