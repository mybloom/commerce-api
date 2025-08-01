package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductListProjection;
import com.loopers.domain.product.ProductRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Page<ProductListProjection> findAllForListViewByBrand(Long brandId, Pageable pageable) {
        return productJpaRepository.findAllForListViewByBrand(brandId, pageable);
    }

    @Override
    public Page<ProductListProjection> findAllForListView(Pageable pageable) {
        return productJpaRepository.findAllForListView(pageable);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public boolean existsListViewableByBrandId(Long brandId) {
        return productJpaRepository.existsListViewableByBrandId(brandId);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll();
    }

    @Override
    public List<Product> findAllById(List<Long> productIds) {
        return productJpaRepository.findAllById(productIds);
    }
}
