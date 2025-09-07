package com.loopers.domain.sharedkernel;

public class PaymentEvent {
    public record PaymentCompleted(Long paymentId, Long orderId) {
    }

    public record PaymentFailed(Long paymentId, Long orderId) {
    }

    public record PaymentInitiated(Long paymentId, Long orderId) {
    }
}
