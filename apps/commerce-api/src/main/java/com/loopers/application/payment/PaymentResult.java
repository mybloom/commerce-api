package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentStatus;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentResult {

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pay {
        private final Long paymentId;
        private final PaymentStatus paymentStatus;

        public static Pay of(Long paymentId) {
            return Pay.builder()
                    .paymentId(paymentId)
                    .paymentStatus(PaymentStatus.CONFIRMED)
                    .build();
        }
    }
}
