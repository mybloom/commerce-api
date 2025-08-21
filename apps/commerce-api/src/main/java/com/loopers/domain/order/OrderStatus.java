package com.loopers.domain.order;

public enum OrderStatus {
    PENDING, //주문 생성
    VALIDATED,
    COMPLETED, //주문 성공
    FAILED //주문 실패
}
