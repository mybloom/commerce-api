package com.loopers.domain.coupon;


import com.loopers.domain.sharedkernel.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponEventHandler {
    private final CouponService couponService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleOrderCompleted(OrderEvent.OrderCompleted event) {
        log.info("쿠폰 사용 처리 이벤트: orderId:{}", event.orderId());
        couponService.applyCoupon(
               CouponCommand.ApplyDiscount.of(event.userId(), event.userCouponIds(), event.orderId())
        );
    }
}
