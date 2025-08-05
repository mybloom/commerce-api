package com.loopers.domain.product;

import java.util.Collections;
import java.util.List;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    public List<Product> findAllValidProducts(Set<Long> productIds) {
        final List<Product> products = productRepository.findAllById(productIds.stream().toList());
        final Set<Long> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());

        if (!foundIds.containsAll(productIds)) {
            return Collections.emptyList();
        }
        return products;
    }

    public void calculateTotal() {

    }
}
