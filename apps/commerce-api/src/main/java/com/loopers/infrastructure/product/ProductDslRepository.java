package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductDslRepository {
    Page<ProductListProjection> findAllForListViewByBrand(Long brandId, Pageable pageable);
    Page<ProductListProjection> findAllForListView(Pageable pageable);
    boolean existsListViewableByBrandId(Long brandId);
}
