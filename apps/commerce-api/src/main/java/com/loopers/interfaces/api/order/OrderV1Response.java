package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderResult;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderV1Response {

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OrderResponse {
        private final boolean duplicateRequest;
        private final Long orderId;
        private final String status;

        public static OrderResponse from(OrderResult.OrderRequestResult result) {
            return OrderResponse.builder()
                    .duplicateRequest(result.isDuplicateRequest())
                    .orderId(result.getOrderId())
                    .status(result.getStatus())
                    .build();
        }
    }
}
