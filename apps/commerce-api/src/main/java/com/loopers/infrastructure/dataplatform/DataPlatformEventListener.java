package com.loopers.infrastructure.dataplatform;


import com.loopers.config.kafka.KafkaTopicsProperties;
import com.loopers.domain.sharedkernel.KafkaRecordFactory;
import com.loopers.domain.sharedkernel.OrderEvent;
import com.loopers.domain.sharedkernel.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Slf4j
@RequiredArgsConstructor
@Component
public class DataPlatformEventListener {

    private static final String TYPE_ID_HEADER = "__TypeId__";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentEvent.PaymentCompleted event) {
        log.info("결제 성공 데이터전송 이벤트: orderId:{}, paymentId:{}",
                event.orderId(), event.paymentId());
        // 파티션 키는 entityId 기준으로 (순서 보장 목적)
        String partitionKey = String.valueOf(event.orderId());
        kafkaTemplate.send(
                KafkaRecordFactory.withTypeHeader(
                        topics.getPaymentEvent(),
                        partitionKey,
                        event
                )
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailed(PaymentEvent.PaymentFailed event) {
        log.info("결제 실패 데이터전송 이벤트: paymentId:{}", event.paymentId());
        // 파티션 키는 entityId 기준으로 (순서 보장 목적)
        String partitionKey = String.valueOf(event.orderId());
        kafkaTemplate.send(
                KafkaRecordFactory.withTypeHeader(
                        topics.getPaymentEvent(),
                        partitionKey,
                        event
                )
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentInitiated(PaymentEvent.PaymentInitiated event) {
        log.info("결제 요청완료 데이터전송 이벤트: paymentId:{}", event.paymentId());
        // 파티션 키는 entityId 기준으로 (순서 보장 목적)
        String partitionKey = String.valueOf(event.orderId());
        kafkaTemplate.send(
                KafkaRecordFactory.withTypeHeader(
                        topics.getPaymentEvent(),
                        partitionKey,
                        event
                )
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPending(OrderEvent.OrderPending event) {
        log.info("주문 초기생성 데이터전송 이벤트: orderId:{}", event.orderId());
        // 파티션 키는 entityId 기준으로 (순서 보장 목적)
        String partitionKey = String.valueOf(event.orderId());
        kafkaTemplate.send(
                KafkaRecordFactory.withTypeHeader(
                        topics.getOrderEvent(),
                        partitionKey,
                        event
                )
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCompleted(OrderEvent.OrderCompleted event) {
        log.info("주문 완료 데이터전송 이벤트: orderId:{}", event.orderId());
        // 파티션 키는 entityId 기준으로 (순서 보장 목적)
        String partitionKey = String.valueOf(event.orderId());
        kafkaTemplate.send(
                KafkaRecordFactory.withTypeHeader(
                        topics.getOrderEvent(),
                        partitionKey,
                        event
                )
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCompleted(OrderEvent.OrderSucceeded event) {
        log.info("주문 성공 데이터전송 이벤트: orderId:{}", event.orderId());
        // 파티션 키는 entityId 기준으로 (순서 보장 목적)
        String partitionKey = String.valueOf(event.orderId());
        kafkaTemplate.send(
                KafkaRecordFactory.withTypeHeader(
                        topics.getOrderEvent(),
                        partitionKey,
                        event
                )
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderFailed(OrderEvent.OrderFailed event) {
        log.info("주문 실패 데이터전송 이벤트: orderId:{}", event.orderId());
        // 파티션 키는 entityId 기준으로 (순서 보장 목적)
        String partitionKey = String.valueOf(event.orderId());
        kafkaTemplate.send(
                KafkaRecordFactory.withTypeHeader(
                        topics.getOrderEvent(),
                        partitionKey,
                        event
                )
        );
    }
}
