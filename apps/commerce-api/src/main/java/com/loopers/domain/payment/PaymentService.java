package com.loopers.domain.payment;

import com.loopers.domain.commonvo.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Component
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment createPaymentByOrderId(final Long orderId, final PaymentMethod paymentMethod, final Money amount) {
        paymentRepository.findByOrderId(orderId)
                .ifPresent(payment -> {
                    throw new CoreException(ErrorType.CONFLICT, "이미 결제 요청이 존재합니다. orderId=" + orderId);
                });

        return paymentRepository.save(
                Payment.createInit(orderId, paymentMethod, amount)
        );
    }

    public void success(
            final Long paymentId, final PaymentMethod paymentMethod, final Money amount
    ) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보가 존재하지 않습니다. paymentId=" + paymentId));

        payment.success();
    }

    @Transactional
    public void fail(Payment payment, String reason) {
        payment.fail();
    }

}
