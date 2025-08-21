package com.loopers.application.order;

import com.loopers.domain.coupon.CouponCommand;
import com.loopers.domain.product.ProductCommand;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderInfo {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Create {
        private final Long userId;
        private final String orderRequestKey;
        private final List<Product> products;
        private final List<Long> userCouponIds;

        // 1. 정적 팩토리 메서드 (생성 관련)
        public static Create of(Long userId, String orderRequestKey, List<Product> products, List<Long> userCouponIds) {
            validateUniqueProductIds(products);
            return new Create(userId, orderRequestKey, products, userCouponIds);
        }

        // 2. 비즈니스 메서드 (변환)
        public ProductCommand.OrderProducts convertToProductCommand() {
            List<ProductCommand.OrderProducts.OrderProduct> commandProducts = this.products.stream()
                    .map(product -> ProductCommand.OrderProducts.OrderProduct.of(
                            product.getProductId(),
                            product.getQuantity()))
                    .toList();

            return ProductCommand.OrderProducts.of(commandProducts);
        }

        public CouponCommand.ApplyDiscount convertToCouponCommand(Long orderAmount) {
            return CouponCommand.ApplyDiscount.of(this.userId, orderAmount, this.userCouponIds);
        }

        // 3. private 유틸리티 메서드
        private static void validateUniqueProductIds(List<Product> products) {
            if (products == null || products.isEmpty()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "상품 목록이 비어있습니다.");
            }

            Set<Long> productIds = new HashSet<>();
            List<Long> duplicateIds = products.stream()
                    .map(Product::getProductId)
                    .filter(id -> !productIds.add(id))
                    .toList();

            if (!duplicateIds.isEmpty()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "중복된 상품ID가 포함되어 있습니다: " + duplicateIds);
            }
        }

        // 4. 내부 클래스 (가장 마지막)
        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Product {
            private final Long productId;
            private final Integer quantity;
        }
    }
}
