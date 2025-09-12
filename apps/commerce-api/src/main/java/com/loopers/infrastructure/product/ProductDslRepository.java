package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductDslRepository {
    Page<ProductListProjection> findAllForListViewByBrand(Long brandId, Pageable pageable);
    Page<ProductListProjection> findAllForListView(Pageable pageable);
    boolean existsListViewableByBrandId(Long brandId);

    List<ProductListProjection> findAllByIdsWithBrand(List<Long> productIds);
}
