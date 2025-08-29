package com.loopers.domain.order;

import com.loopers.domain.sharedkernel.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderEventHandler {

    private final OrderService orderService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleOrderCompleted(PaymentEvent.PaymentCompleted event) {
        log.info("주문 성공 처리 이벤트: {}", event.orderId());
        orderService.success(event.orderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleOrderFailed(PaymentEvent.PaymentFailed event) {
        log.info("주문 실패 처리 이벤트: orderId:{}", event.orderId());
        orderService.markFailed(event.orderId());
    }
}
