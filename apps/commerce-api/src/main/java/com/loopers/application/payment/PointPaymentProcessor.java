package com.loopers.application.payment;

import com.loopers.application.payment.dto.PaymentInfo;
import com.loopers.application.payment.dto.PaymentResult;
import com.loopers.domain.order.Order;

public interface PointPaymentProcessor {
    PaymentResult.Pay process(PaymentInfo.PointPay info, Order order);
}
