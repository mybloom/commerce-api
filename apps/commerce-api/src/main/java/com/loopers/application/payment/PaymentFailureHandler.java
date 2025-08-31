package com.loopers.application.payment;

import com.loopers.application.payment.dto.PaymentFailureInfo;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.sharedkernel.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentFailureHandler {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailedPointPayment(PaymentFailureInfo.Fail info) {
        //쿠폰 사용 처리 복구
        couponService.restoreByUser(info.orderId(), info.userId());

        //결제 실패 저장 : 포인트의 경우 pending 상태 없이 실패 상태로 저장
        Payment payment = paymentService.createFailedPayment(info.convertToCommand());

        //주문 실패 처리(이벤트)
        PaymentEvent.PaymentFailed event = new PaymentEvent.PaymentFailed(payment.getId(), info.orderId());
        eventPublisher.publishEvent(event);
    }

    @Transactional
    public void handleFailedCardPaymentCallback(PaymentFailureInfo.CardCallbackFail info) {
        //쿠폰 사용 처리 복구
        couponService.restore(info.orderId());

        //결제 실패 처리
        Payment payment = paymentService.failViaCallback(info.orderId(), info.transactionKey(), info.failureReason());

        //결제 실패 처리(이벤트)
        PaymentEvent.PaymentFailed event = new PaymentEvent.PaymentFailed(payment.getId(), info.orderId());
        eventPublisher.publishEvent(event);
    }

    @Transactional
    public void handleFailedCardPayment(
            PaymentFailureInfo.Fail info
    ) {
        //쿠폰 사용 처리 복구
        couponService.restore(info.orderId());

        //결제 실패 저장 : 실패 상태로 처음 저장
        Payment payment = paymentService.createFailedCardPayment(info.convertToCommand());

        //결제 실패 처리(이벤트)
        PaymentEvent.PaymentFailed event = new PaymentEvent.PaymentFailed(payment.getId(), info.orderId());
        eventPublisher.publishEvent(event);
    }
}
