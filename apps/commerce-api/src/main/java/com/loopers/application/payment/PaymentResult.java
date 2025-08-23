package com.loopers.application.payment;

public final class PaymentResult {
    private PaymentResult() {}

    public static record Pay(
            Long paymentId,
//            String paymentStatus,
            Long orderId,
            PaymentProcessResult outcome
    ) {
        public static Pay of(Long paymentId, Long orderId, PaymentProcessResult outcome) {
            return new Pay(paymentId, orderId, outcome);
        }
    }
}
