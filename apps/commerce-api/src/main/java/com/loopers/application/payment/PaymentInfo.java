package com.loopers.application.payment;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.payment.PayMethod;

public class PaymentInfo {
    public record Pay(
            Long orderId,
            PayMethod payMethod
    ){}
}
