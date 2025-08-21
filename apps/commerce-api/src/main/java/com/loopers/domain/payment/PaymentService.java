package com.loopers.domain.payment;

import com.loopers.domain.commonvo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Long saveSuccess(
            final Long orderId, final PaymentMethod paymentMethod, final Money amount
    ) {
        final Payment payment = paymentRepository.save(
                Payment.confirmSuccess(orderId, paymentMethod, amount, PaymentStatus.CONFIRMED)
        );

        return payment.getId();
    }

    public Long saveFailure(
            final Long orderId,
            final PaymentMethod paymentMethod,
//            final Money amount,
            final PaymentFailureReason failureReason
    ) {
        final Payment payment = paymentRepository.save(
                Payment.confirmFailure(
                        orderId, paymentMethod,PaymentStatus.CANCELED, failureReason
                )
        );

        return payment.getId();
    }
}
