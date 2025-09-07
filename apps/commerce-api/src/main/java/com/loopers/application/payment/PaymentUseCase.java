package com.loopers.application.payment;


import com.loopers.application.payment.dto.*;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentUseCase {
    private final OrderService orderService;

    private final CardPaymentCallbackHandler cardPaymentCallbackHandler;
    private final PaymentFailureHandler failureHandler;
    private final PaymentProcessorFactory paymentProcessorFactory;

    public PaymentResult.Pay pay(final PaymentInfo.Pay info) {
        // 1) 기존 주문 정보 조회
        //todo: order를 전달했을 때, 변경 가능성.
        Order order = orderService.getUserOrderWithLinesByUser(info.getUserId(), info.getOrderId());
        if(order.getStatus() != OrderStatus.COMPLETED){
            throw new CoreException(ErrorType.CONFLICT, "결제 가능한 상태가 아닙니다. 현재 상태: " + order.getStatus());
        }

        // 2) 결제 처리
        PaymentProcessor processor =
                paymentProcessorFactory.getProcessor(info.getPaymentMethod());
        PaymentResult.Pay result = processor.process(info, order);

        return PaymentResult.Pay.of(result.paymentId(), result.paymentStatus(), info.getOrderId());
    }

    public void pgConclude(PaymentCallbackInfo.ProcessTransaction info) {
        // todo: pg요청 : txId로 실제 요청에 대한 콜백이 맞는지 확인

        //1. 성공일 때
        if (info.status().equals(PgProcessStatus.SUCCESS)) {
            log.info("********PG 콜백 성공 처리 - orderId: {}, transactionKey: {}", info.orderId(), info.transactionKey());
            cardPaymentCallbackHandler.success(info);
        }

        //2. 실패일 때
        if (info.status().equals(PgProcessStatus.FAILED)) {
            log.info("********PG 콜백 실패 처리 - orderId: {}, transactionKey: {}, reason: {}", info.orderId(), info.transactionKey(), info.reason());
            failureHandler.handleFailedCardPaymentCallback(
                    PaymentFailureInfo.CardCallbackFail.of(info)
            );
        }

    }

}
