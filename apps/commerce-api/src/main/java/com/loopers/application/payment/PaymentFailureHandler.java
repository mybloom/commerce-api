package com.loopers.application.payment;

import com.loopers.application.payment.dto.PaymentFailureInfo;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentFailureHandler {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final CouponService couponService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailedPointPayment(PaymentFailureInfo.Fail info) {
        //쿠폰 사용 처리 복구
        couponService.restoreByUser(info.orderId(), info.userId());

        //결제 실패 저장 : 포인트의 경우 pending 상태 없이 실패 상태로 저장
        paymentService.createFailedPayment(info.convertToCommand());

        //주문 실패 처리
        orderService.markFailed(info.orderId());
    }

    @Transactional
    public void handleFailedCardPaymentCallback(PaymentFailureInfo.CardCallbackFail info) {
        //쿠폰 사용 처리 복구
        couponService.restore(info.orderId());

        //결제 실패 처리
        paymentService.failViaCallback(info.orderId(), info.transactionKey(), info.failureReason());

        //주문 실패 처리
        orderService.markFailed(info.orderId());
    }

    @Transactional
    public void handleFailedCardPayment(
            PaymentFailureInfo.Fail info
    ) {
        //쿠폰 사용 처리 복구
        couponService.restore(info.orderId());

        //결제 실패 저장 : 실패 상태로 처음 저장
        paymentService.createFailedCardPayment(info.convertToCommand());

        //주문 실패 처리
        orderService.markFailed(info.orderId());
    }
}
