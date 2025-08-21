package com.loopers.application.order;

import com.loopers.domain.order.Order;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderResult {

    @ToString
    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OrderRequestResult {
        private final boolean duplicateRequest;
        private final Long orderId;
        private final String status;

        public static OrderRequestResult alreadyOrder(Order existingOrder) {
            return OrderRequestResult.builder()
                    .duplicateRequest(true)
                    .orderId(existingOrder.getId())
                    .status(existingOrder.getStatus().name())
                    .build();
        }

        public static OrderRequestResult completedOrder(Order createdOrder) {
            return OrderRequestResult.builder()
                    .duplicateRequest(false)
                    .orderId(createdOrder.getId())
                    .status(createdOrder.getStatus().name())
                    .build();
        }
    }
}
