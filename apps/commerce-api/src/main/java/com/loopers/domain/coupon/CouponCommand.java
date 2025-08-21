package com.loopers.domain.coupon;

import com.loopers.domain.commonvo.Money;
import lombok.*;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CouponCommand {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ApplyDiscount {
        private final Long userId;
        private final Money orderAmount;
        private final List<Long> userCouponIds;

        public static ApplyDiscount of(Long userId, Long amount, List<Long> userCouponIds) {
            return ApplyDiscount.builder()
                    .userId(userId)
                    .orderAmount(Money.of(amount))
                    .userCouponIds(userCouponIds)
                    .build();
        }
    }
}
