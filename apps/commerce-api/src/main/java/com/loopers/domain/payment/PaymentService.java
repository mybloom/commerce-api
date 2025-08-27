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
    private final CardPaymentRepository cardPaymentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment createPaymentByOrderId(final Long userId, final Long orderId, final PaymentMethod paymentMethod,
                                          final Money amount) {
        paymentRepository.findByOrderIdAndUserId(orderId, userId)
                .ifPresent(payment -> {
                    throw new CoreException(ErrorType.CONFLICT, "이미 결제 요청이 존재합니다. orderId=" + orderId);
                });

        return paymentRepository.save(
                Payment.createInit(userId, orderId, paymentMethod, amount)
        );
    }

    public Payment createSuccess(final Long userId, final Long orderId, final PaymentMethod paymentMethod, final Money amount
    ) {
        return paymentRepository.save(Payment.createSuccess(userId, orderId, paymentMethod, amount));
    }

    public void fail(Long paymentId, String failureReason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보가 존재하지 않습니다."));
        payment.fail(failureReason);
    }

    public void createFailedPayment(final PaymentCommand.SaveFail command) {
        paymentRepository.save(
                Payment.createFail(command.userId(), command.orderId(), command.paymentMethod(), command.amount(),
                        command.paymentFailureReason())
        );
    }

    public void failViaCallback(final Long orderId, final String transactionalKey, final String failureReason) {
        final Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보가 존재하지 않습니다."));
        payment.fail(failureReason);

        final CardPayment cardPayment = cardPaymentRepository.findByPaymentIdAndTransactionKey(payment.getId(), transactionalKey)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "카드 결제 정보가 존재하지 않습니다. paymentId=" + payment.getId()));
        cardPayment.fail(failureReason);
    }

    public void completeViaCallback(final Long orderId, final String transactionKey, final String reason) {
        final Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.CONFLICT, "결제 정보가 존재하지 않습니다. orderId=" + orderId));
        payment.success();

        final CardPayment cardPayment = cardPaymentRepository.findByPaymentIdAndTransactionKey(payment.getId(), transactionKey)
                .orElseThrow(() -> new CoreException(ErrorType.CONFLICT, "카드 결제 정보가 존재하지 않습니다. paymentId=" + payment.getId() + ", transactionKey=" + transactionKey));
        cardPayment.success(reason);
    }

    @Transactional
    public Payment createCardPayment(PaymentCommand.SaveCard command) {
        final Payment payment = paymentRepository.save(
                Payment.createInit(command.userId(), command.orderId(), command.paymentMethod(), command.amount())
        );

        cardPaymentRepository.save(
                CardPayment.createInit(payment.getId(), command.transactionKey())
        );

        return payment;
    }

    public void createFailedCardPayment(PaymentCommand.SaveFail command) {
        paymentRepository.save(
                Payment.createFail(
                        command.userId(), command.orderId(), command.paymentMethod(), command.amount(), command.paymentFailureReason()
                )
        );
    }
}
