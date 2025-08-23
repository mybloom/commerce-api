package com.loopers.application.payment;


import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class PaymentUseCase {
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final PaymentFailureHandler failureHandler;

    private final PaymentProcessorFactory paymentProcessorFactory;

    @Transactional
    public PaymentResult.Pay pay(final PaymentInfo.Pay info) {
        // 1) 기존 결제 정보 조회. 없으면 PENDING 생성
        Order order = orderService.getUserOrderWithLock(info.getUserId(), info.getOrderId());
        Payment payment = paymentService.createPaymentByOrderId(
                info.getOrderId(),
                info.getPaymentMethod(),
                order.getPaymentAmount()
        );

        // 2) 수단별 Processor 호출
        PaymentProcessor processor =
                paymentProcessorFactory.getProcessor(info.getPaymentMethod());
        PaymentProcessResult paymentProcessResult = processor.process(info, payment, order.getPaymentAmount());

        // 3) 결과 해석 + 상태 전이 (UseCase 책임)
        if (paymentProcessResult instanceof PaymentProcessResult.Approved result) {
            return PaymentResult.Pay.of(payment.getId(),info.getOrderId(), paymentProcessResult);
        }

        if (paymentProcessResult instanceof PaymentProcessResult.Pending result) {
            return PaymentResult.Pay.of(payment.getId(), info.getOrderId(), paymentProcessResult);
        }

        //todo: 실패 처리
        if (paymentProcessResult instanceof PaymentProcessResult.Declined result) {
            paymentService.fail(payment, result.reason());
            return PaymentResult.Pay.of(payment.getId(), info.getOrderId(), paymentProcessResult);
        }

        throw new CoreException(ErrorType.INTERNAL_ERROR, "!!!Unknown result type: " + paymentProcessResult);
    }

}
