package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentMethod;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentInfo {

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pay {
        private final Long userId;
        private final Long orderId;
        private final PaymentMethod paymentMethod;

        public static Pay of(Long userId, Long orderId, String paymentMethod) {
            return Pay.builder()
                    .userId(userId)
                    .orderId(orderId)
                    .paymentMethod(PaymentMethod.valueOf(paymentMethod.toUpperCase()))
                    .build();
        }
    }
}
