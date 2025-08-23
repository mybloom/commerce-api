package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentFailureReason;
import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentFailureHandler {

    private final PaymentService paymentService;

    /*@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(Long userId, PaymentInfo.Pay payInfo, PaymentFailureReason paymentFailureReason) {
        paymentService.saveFailure(
                payInfo.getOrderId(),
                payInfo.getPaymentMethod(),
                paymentFailureReason
        );
    }*/
}
