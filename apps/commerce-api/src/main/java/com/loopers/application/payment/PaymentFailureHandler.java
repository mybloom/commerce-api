package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentFailureReason;
import com.loopers.domain.payment.PaymentService;
import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentFailureHandler {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(Long userId, PaymentInfo.Pay payInfo, PaymentFailureReason paymentFailureReason) {
        Order order = orderService.getUserOrder(userId, payInfo.orderId());
        boolean isOrderConfirmed = false;
        orderService.finalizeOrderResult(order, isOrderConfirmed);

        paymentService.saveFailure(
                payInfo.orderId(),
                payInfo.paymentMethod(),
                order.getPaymentAmount(),
                paymentFailureReason
        );
    }
}
