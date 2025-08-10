package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderStatus;

public class OrderResult {

    public record OrderRequestResult(
        boolean duplicateRequest,
        Long orderId,
        OrderStatus status
    ) {
        public static OrderRequestResult alreadyOrder(Order existingOrder) {
            return new OrderRequestResult(
                true,
                existingOrder.getId(),
                existingOrder.getStatus()
            );
        }

        public static OrderRequestResult from(Order createdOrder) {
            return new OrderRequestResult(
                false,
                createdOrder.getId(),
                createdOrder.getStatus()
            );
        }
    }
}
