package com.loopers.domain.product;

import java.util.*;

import com.loopers.application.product.ProductDetailCachePolicy;
import com.loopers.support.cache.CacheTemplate;
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
    private final CacheTemplate cache;
    private final ProductDetailCachePolicy detailPolicy;

    public Page<ProductListProjection> retrieveListByBrand(final Long brandId, final Pageable pageable) {
        return productRepository.findAllForListViewByBrand(brandId, pageable);
    }

    public Page<ProductListProjection> retrieveList(final Pageable pageable) {
        return productRepository.findAllForListView(pageable);
    }

    public Optional<Product> retrieveOne(Long productId) {
        return productRepository.findById(productId);
    }

    public Product retrieveOneByCacheOld(Long productId) {
        final String key = detailPolicy.getKeyFormat().formatted(productId);

        // 캐시에 Product 자체를 저장/조회 (주의: JPA Lazy 필드 있으면 DTO/Snapshot 권장)
        Product product = cache.getOrLoad(
                detailPolicy.getCacheName(),
                key,
                Product.class,
                () -> productRepository.findById(productId).orElse(null), // 미스 시 DB
                detailPolicy.getTtl(),
                detailPolicy.getNullTtl()
        );

        if (product == null) {
            // null 마커는 캐시에 짧게 들어감(nullTtl), 호출자는 예외로 처리
            throw new CoreException(ErrorType.NOT_FOUND, "유효한 상품을 찾을 수 없습니다.");
        }
        return product;
    }

    public ProductQuery.ProductDetailQuery retrieveOneByCache(Long productId) {
        String key = detailPolicy.getKeyFormat().formatted(productId);

        ProductQuery.ProductDetailQuery query = cache.getOrLoad(
                detailPolicy.getCacheName(),
                key,
                ProductQuery.ProductDetailQuery.class,
                () -> productRepository.findById(productId) // 캐시 미스 시 DB
                        .map(ProductQuery.ProductDetailQuery::from)
                        .orElse(null),
                detailPolicy.getTtl(),
                detailPolicy.getNullTtl()
        );

        return query;
    }

    public void increaseLikeCountAtomically(final Product product) {
        int processCount = productRepository.updateLikeCountById(product.getId());
    }

    public void decreaseLikeCount(final Product product) {
        productRepository.decreaseLikeCountById(product.getId());
    }

    public List<Product> getProducts(List<Long> productIds) {
        return productRepository.findAllByIds(productIds);
    }

    public List<Product> findAllValidProductsOrThrow(List<Long> productIds) {
        List<Product> products = productRepository.findAllValidWithPessimisticLock(productIds);

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
