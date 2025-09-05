package com.loopers.domain.sharedkernel;

import java.util.List;

public class OrderEvent {
    public record OrderPending(Long orderId) {
    }

    public record OrderCompleted(
            Long orderId,
            Long userId,
            Long orderAmount,
            Long paymentAmount,
            List<Long> userCouponIds
    ) {
    }

    public record OrderSucceeded(Long orderId) {
    }

    public record OrderFailed(Long orderId) {
    }
}
