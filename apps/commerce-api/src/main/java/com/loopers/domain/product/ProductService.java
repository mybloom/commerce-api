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

    public List<Product> findAllValidProductsOrThrow(Set<Long> productIds) {
        List<Product> products = productRepository.findAllByIds(productIds.stream().toList());

        Set<Long> foundIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toSet());

        if (!foundIds.containsAll(productIds)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다.");
        }

        return products;
    }

    /*public boolean deductStock(List<ProductCommand.CheckStock> commands) {
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
                return false;
            }
        }
        return true;
    }*/
    public boolean deductStock(List<ProductCommand.CheckStock> commands) {
        Set<Long> productIds = commands.stream()
                .map(ProductCommand.CheckStock::productId)
                .collect(Collectors.toSet());

        List<Product> products = productRepository.findAllByIds(productIds.stream().toList());

        // 1. 재고 검증 (재고 수량 변경 없이)
        for (ProductCommand.CheckStock command : commands) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(command.productId()))
                    .findFirst()
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품 없음: " + command.productId()));

            if (command.quantity().isGreaterThan(product.getStockQuantity())) {
                product.markSoldOut(); // 일시품절 처리 todo: 비동기 처리 필요
                return false;
            }
        }

        // 2. 재고 차감 (모든 검증 통과 후)
        for (ProductCommand.CheckStock command : commands) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(command.productId()))
                    .findFirst()
                    .get(); // 이미 검증됐으므로 get() 가능

            product.deductStock(command.quantity()); // 이 시점에만 상태 변경
        }

        return true;
    }

}
