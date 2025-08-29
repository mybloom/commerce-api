package com.loopers.domain.sharedkernel;

import com.loopers.domain.commonvo.Money;

import java.util.List;

public class OrderEvent {
    public record OrderPending(Long orderId) {
    }

    public record OrderCompleted(
            Long orderId,
            Long userId,
            Money orderAmount,
            List<Long> userCouponIds
    ) {
    }

    public record OrderSucceeded(Long orderId) {
    }

    public record OrderFailed(Long orderId) {
    }
}
