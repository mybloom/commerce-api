package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentStatus;

public class PaymentResult {
    public record Pay(
            Long paymentId,
            PaymentStatus paymentStatus
    ) {
        public static Pay of(Long paymentId, boolean isPaymentConfirmed) {
            PaymentStatus paymentStatus = isPaymentConfirmed ? PaymentStatus.CONFIRMED : PaymentStatus.CANCELED;
            return new Pay(paymentId, paymentStatus);
        }
    }
}
