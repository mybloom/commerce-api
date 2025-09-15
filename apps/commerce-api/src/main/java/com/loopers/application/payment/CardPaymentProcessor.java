package com.loopers.application.payment;

import com.loopers.application.payment.dto.PaymentInfo;
import com.loopers.application.payment.dto.PaymentResult;
import com.loopers.domain.order.Order;

public interface CardPaymentProcessor {
    PaymentResult.Pay process(PaymentInfo.CardPay info, Order order);
}
