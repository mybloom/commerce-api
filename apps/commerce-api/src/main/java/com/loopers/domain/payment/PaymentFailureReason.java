package com.loopers.domain.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentFailureReason {
    OUT_OF_STOCK("재고 부족"),
    INSUFFICIENT_BALANCE("잔액 부족"),
    INVALID_PAYMENT_DETAILS("잘못된 결제 정보"),
    ORDER_NOT_FOUND("주문을 찾을 수 없음");

    private final String message;
}
