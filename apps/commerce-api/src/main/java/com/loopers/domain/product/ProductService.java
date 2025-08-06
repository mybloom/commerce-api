package com.loopers.domain.product;

import java.util.*;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
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

    public void increaseLikeCount(final Product product) {
        product.increaseLikeCount();
        productRepository.save(product);
    }

    public void decreaseLikeCount(final Product product) {
        product.decreaseLikeCount();
        productRepository.save(product);
    }

    public List<Product> getProducts(List<Long> productIds) {
        return productRepository.findAllByIds(productIds);
    }

    public List<Product> findAllValidProducts(Set<Long> productIds) {
        final List<Product> products = productRepository.findAllByIds(productIds.stream().toList());
        final Set<Long> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());

        if (!foundIds.containsAll(productIds)) {
            return Collections.emptyList();
        }
        return products;
    }

    public void deductStock(List<ProductCommand.CheckStock> commands) {
        Set<Long> productIds = commands.stream().map(ProductCommand.CheckStock::productId).collect(Collectors.toSet());
        List<Product> products = productRepository.findAllByIds(productIds.stream().toList());

        for (ProductCommand.CheckStock command : commands) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(command.productId()))
                    .findFirst()
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품 없음: " + command.productId()));

            try {
                product.deductStock(command.quantity());
            } catch (CoreException e) {
                // TODO: 재고 부족 시 상품 상태 일시품절 처리 (비동기)
                product.markSoldOut();
                throw new CoreException(ErrorType.CONFLICT, "재고 부족: " + command.productId());
            }
        }
    }

}
