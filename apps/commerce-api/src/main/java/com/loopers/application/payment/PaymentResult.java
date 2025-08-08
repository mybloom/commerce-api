package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentStatus;

public class PaymentResult {
    public record Pay(
            Long paymentId,
            PaymentStatus paymentStatus
    ) {
        public static Pay of(Long paymentId) {
            return new Pay(paymentId, PaymentStatus.CONFIRMED);
        }
    }
}
