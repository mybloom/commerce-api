package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderInfo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderV1Request {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Create {
        @NotEmpty
        private final List<Product> products;
        private final List<@NotNull @Positive Long> userCouponIds;

        public OrderInfo.Create convertToOrderInfo(Long userId, String orderRequestKey) {
            List<OrderInfo.Create.Product> orderInfoProducts = this.products.stream()
                    .map(Product::convertToOrderInfoProduct)
                    .toList();

            return OrderInfo.Create.of(userId, orderRequestKey, orderInfoProducts, this.userCouponIds);
        }

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Product {
            private final Long productId;
            private final Integer quantity;

            private OrderInfo.Create.Product convertToOrderInfoProduct() {
                return OrderInfo.Create.Product.builder()
                        .productId(this.productId)
                        .quantity(this.quantity)
                        .build();
            }
        }
    }
}
