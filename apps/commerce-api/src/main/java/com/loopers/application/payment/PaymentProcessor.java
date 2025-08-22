package com.loopers.application.payment;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentMethod;

public interface PaymentProcessor {
    PaymentProcessResult process(PaymentInfo.Pay info, Payment payment, Money payAmount);

    boolean supports(PaymentMethod paymentMethod);
}
