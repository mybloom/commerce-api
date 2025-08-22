package com.loopers.domain.product;

import java.math.BigDecimal;
import java.util.*;

import com.loopers.application.order.OrderInfo;
import com.loopers.application.product.ProductDetailCachePolicy;
import com.loopers.domain.commonvo.Money;
import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.order.OrderCommand;
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

    public List<Product> validateProductsAndStock(ProductCommand.OrderProducts command) {
        // 1. 상품 ID 추출
        List<Long> commandProductIds = command.getProducts().stream()
                .map(ProductCommand.OrderProducts.OrderProduct::getProductId)
                .toList();

        // 2. 상품 존재 여부 확인 (비관적 락으로 조회)
        List<Product> products = productRepository.findAllValidWithPessimisticLock(commandProductIds);

        // 3. 상품 ID 유효성 검증
        if (products.size() != commandProductIds.size()) {
            throw new CoreException(ErrorType.NOT_FOUND, "주문 불가능한 상품이 포함되어 있습니다.");
        }

        // 4. 재고 검증
        Map<Long, Quantity> productQuntityMap = command.getProducts().stream()
                .collect(Collectors.toMap(
                        ProductCommand.OrderProducts.OrderProduct::getProductId,
                        ProductCommand.OrderProducts.OrderProduct::getQuantity
                ));

         for (Product product : products) {
            Quantity quantity = productQuntityMap.get(product.getId());

             if (quantity.isGreaterThan(product.getStockQuantity())) {
                throw new CoreException(ErrorType.CONFLICT,
                        String.format("상품 [%s]의 재고가 부족합니다. 주문수량: %d, 재고수량: %d",
                                product.getName(), quantity.getAmount(), product.getStockQuantity().getAmount()));
            }
          }

        return products;
    }

    public Money calculateTotalAmount(List<Product> products, ProductCommand.OrderProducts orderProducts) {
        // productId를 키로 하는 수량 맵 생성
        Map<Long, Quantity> quantityMap = orderProducts.getProducts().stream()
                .collect(Collectors.toMap(
                        ProductCommand.OrderProducts.OrderProduct::getProductId,
                        ProductCommand.OrderProducts.OrderProduct::getQuantity
                ));

        // 각 상품별로 (가격 × 수량) 계산하여 총합 반환
        return products.stream()
                .map(product -> {
                    Quantity orderQuantity = quantityMap.get(product.getId());
                    return product.getPrice().multiply(orderQuantity);
                })
                .reduce(Money.ZERO, Money::add);
    }

    public boolean deductStockOld(final List<ProductCommandOld.DeductStock> commands) {
        // 1. 재고 검증 (재고 수량 변경 없이)
        for (ProductCommandOld.DeductStock command : commands) {
            Product product = command.product();
            if (command.quantity().isGreaterThan(product.getStockQuantity())) {
                product.markSoldOut();
                return false;
            }
        }

        // 2. 재고 차감 (모든 검증 통과 후)
        for (ProductCommandOld.DeductStock command : commands) {
            Product product = command.product();
            product.deductStock(command.quantity());
        }

        return true;
    }

    public List<Product> deductStock(ProductCommand.DeductStocks command) {
        // 1. 상품 ID 추출
        List<Long> commandProductIds = command.getStocks().stream()
                .map(ProductCommand.DeductStocks.DeductStock::getProductId)
                .toList();

        // 2. 상품 존재 여부 확인 (비관적 락으로 조회)
        List<Product> products = productRepository.findAllValidWithPessimisticLock(commandProductIds);

        // 3. 상품 ID 유효성 검증
        if (products.size() != commandProductIds.size()) {
            throw new CoreException(ErrorType.NOT_FOUND, "주문 불가능한 상품이 포함되어 있습니다.");
        }

        // 4. 재고 차감
        Map<Long, Quantity> productQuntityMap = command.getStocks().stream()
                .collect(Collectors.toMap(
                        ProductCommand.DeductStocks.DeductStock::getProductId,
                        ProductCommand.DeductStocks.DeductStock::getQuantity
                ));

        for (Product product : products) {
            Quantity quantity = productQuntityMap.get(product.getId());

            product.deductStock(quantity);
        }

        return products;
    }
}
