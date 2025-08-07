package com.loopers.domain.payment;

import com.loopers.domain.commonvo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Long save(
            Long orderId, PaymentMethod paymentMethod, Money amount, boolean isPaymentConfirmed
    ) {
        PaymentStatus paymentStatus = isPaymentConfirmed ? PaymentStatus.CONFIRMED : PaymentStatus.CANCELED;
        final Payment payment = paymentRepository.save(Payment.confirm(orderId, paymentMethod, amount, paymentStatus));

        return payment.getId();
    }
}
