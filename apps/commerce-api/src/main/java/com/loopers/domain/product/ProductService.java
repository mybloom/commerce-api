package com.loopers.domain.product;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductListProjection> retrieveListByBrand(final Long brandId, final Pageable pageable) {
        return productRepository.findAllForListViewByBrand(brandId, pageable);
    }

    public Page<ProductListProjection> retrieveList(final Pageable pageable) {
        return productRepository.findAllForListView(pageable);
    }

    public Optional<Product> retrieveOne(Long productId) {
        return productRepository.findById(productId);
    }

    public void increaseLikeCount(final Product product) {
        product.increaseLikeCount();
        productRepository.save(product);
    }

    public void decreaseLikeCount(final Product product) {
        product.decreaseLikeCount();
        productRepository.save(product);
    }

    public List<Product> getProducts(List<Long> productIds) {
        return productRepository.findAllById(productIds);
    }
}
