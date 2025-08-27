package com.loopers.application.payment.dto;

public final class PaymentResult {
    private PaymentResult() {}

    public record Pay(
            Long paymentId,
            String paymentStatus,
            Long orderId
    ) {
        public static Pay of(Long paymentId, String paymentStatus, Long orderId) {
            return new Pay(paymentId, paymentStatus, orderId);
        }
    }
}
