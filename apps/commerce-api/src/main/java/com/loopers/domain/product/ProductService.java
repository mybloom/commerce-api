package com.loopers.domain.product;

import java.util.*;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

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

    public void increaseLikeCountAtomically(final Product product) {
        int processCount = productRepository.updateLikeCountById(product.getId());
    }

    public void decreaseLikeCount(final Product product) {
        product.decreaseLikeCount();
    }

    public List<Product> getProducts(List<Long> productIds) {
        return productRepository.findAllByIds(productIds);
    }

    public List<Product> findAllValidProductsOrThrow(List<Long> productIds) {
        List<Product> products = productRepository.findAllByIds(productIds);

        Set<Long> foundIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toSet());

        if (!foundIds.containsAll(productIds)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다.");
        }

        return products;
    }

    public boolean deductStock(final List<ProductCommand.DeductStock> commands) {
        // 1. 재고 검증 (재고 수량 변경 없이)
        for (ProductCommand.DeductStock command : commands) {
            Product product = command.product();
            if (command.quantity().isGreaterThan(product.getStockQuantity())) {
                product.markSoldOut();
                return false;
            }
        }

        // 2. 재고 차감 (모든 검증 통과 후)
        for (ProductCommand.DeductStock command : commands) {
            Product product = command.product();
            product.deductStock(command.quantity());
        }

        return true;
    }

}
