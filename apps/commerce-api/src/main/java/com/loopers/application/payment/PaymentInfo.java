package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentMethod;

public class PaymentInfo {
    public record Pay(
            Long orderId,
            PaymentMethod paymentMethod
    ){}
}
