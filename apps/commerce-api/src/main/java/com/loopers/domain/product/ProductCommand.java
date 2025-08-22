package com.loopers.domain.product;

import com.loopers.domain.commonvo.Quantity;
import lombok.*;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductCommand {

    /**
     * 주문 상품 정보
     */
    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OrderProducts {
        private final List<OrderProduct> products;

        public static OrderProducts of(List<OrderProduct> products) {
            return OrderProducts.builder()
                    .products(products)
                    .build();
        }

        @Getter
        @Builder(access = AccessLevel.PRIVATE)
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class OrderProduct {
            private final Long productId;
            private final Quantity quantity;

            public static OrderProduct of(Long productId, Integer quantity) {
                return OrderProduct.builder()
                        .productId(productId)
                        .quantity(Quantity.of(quantity))
                        .build();
            }
        }
    }

    /**
     * 재고 차감
     */
    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DeductStocks {
        private final List<DeductStock> stocks;

        public static DeductStocks of(List<DeductStock> stocks) {
            return DeductStocks.builder()
                    .stocks(stocks)
                    .build();
        }

        @Getter
        @Builder(access = AccessLevel.PRIVATE)
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class DeductStock {
            private final Long productId;
            private final Quantity quantity;

            public static DeductStock of(Long productId, Quantity quantity) {
                return DeductStock.builder()
                        .productId(productId)
                        .quantity(quantity)
                        .build();
            }
        }
    }
}
