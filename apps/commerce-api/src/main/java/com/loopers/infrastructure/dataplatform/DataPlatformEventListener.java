package com.loopers.infrastructure.dataplatform;


import com.loopers.domain.sharedkernel.OrderEvent;
import com.loopers.domain.sharedkernel.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Slf4j
@RequiredArgsConstructor
@Component
public class DataPlatformEventListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentEvent.PaymentCompleted event) {
        log.info("결제 성공 데이터전송 이벤트: orderId:{}, paymentId:{}",
                event.orderId(), event.paymentId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailed(PaymentEvent.PaymentFailed event) {
        log.info("결제 실패 데이터전송 이벤트: paymentId:{}", event.paymentId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentInitiated(PaymentEvent.PaymentInitiated event) {
        log.info("결제 요청완료 데이터전송 이벤트: paymentId:{}", event.paymentId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPending(OrderEvent.OrderPending event) {
        log.info("주문 초기생성 데이터전송 이벤트: orderId:{}", event.orderId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCompleted(OrderEvent.OrderCompleted event) {
        log.info("주문 완료 데이터전송 이벤트: orderId:{}", event.orderId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderSucceeded(OrderEvent.OrderSucceeded event) {
        log.info("주문 성공 데이터전송 이벤트: orderId:{}", event.orderId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderFailed(OrderEvent.OrderFailed event) {
        log.info("주문 실패 데이터전송 이벤트: orderId:{}", event.orderId());
    }
}
