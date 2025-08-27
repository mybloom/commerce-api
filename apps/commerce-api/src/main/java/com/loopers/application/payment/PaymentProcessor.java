package com.loopers.application.payment;

import com.loopers.application.payment.dto.PaymentInfo;
import com.loopers.application.payment.dto.PaymentResult;
import com.loopers.domain.order.Order;
import com.loopers.domain.payment.PaymentMethod;

public interface PaymentProcessor {
    PaymentResult.Pay process(PaymentInfo.Pay info, Order order);

    boolean supports(PaymentMethod paymentMethod);
}
