package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
    Page<ProductListProjection> findAllForListView(Pageable pageable);
    Page<ProductListProjection> findAllForListViewByBrand(Long brandId, Pageable pageable);
    Optional<Product> findById(Long id);

    Product save(Product product);

    boolean existsListViewableByBrandId(Long brandId);

    List<Product> findAll();

    List<Product> findAllByIds(List<Long> productIds);
}
