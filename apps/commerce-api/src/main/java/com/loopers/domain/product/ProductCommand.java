package com.loopers.domain.product;

import com.loopers.domain.commonvo.Quantity;
import lombok.*;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductCommand {

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
}
