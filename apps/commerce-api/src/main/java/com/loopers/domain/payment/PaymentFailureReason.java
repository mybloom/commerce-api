package com.loopers.domain.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
@Getter
@RequiredArgsConstructor
public enum PaymentFailureReason {
    OUT_OF_STOCK("재고 부족"),
    INSUFFICIENT_POINT("포인트 잔액 부족"),
    INVALID_PAYMENT_DETAILS("잘못된 결제 정보"),
    ORDER_NOT_FOUND("주문을 찾을 수 없음"),
    UNKNOWN("알 수 없는 오류"),
    PG_COMMUNICATION_ERROR("PG사 통신 오류");

    private final String message;

    public static PaymentFailureReason fromMessage(String msg) {
        return Arrays.stream(values())
                .filter(r -> msg.contains(r.getMessage()))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
