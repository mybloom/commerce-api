package com.loopers.application.payment;

import com.loopers.support.error.CardPaymentException;

public interface ExternalCardService {
    /**
     * PG사에 카드 결제 요청
     * @param cardNumber 카드 번호
     * @param cardType 카드 타입 (VISA, MASTER 등)
     * @param orderId 주문 ID
     * @return PG사에서 발급한 외부 결제 ID
     * @throws CardPaymentException 결제 요청 실패 시
     */
    String requestPayment(String cardNumber, String cardType, Long orderId) throws CardPaymentException;
}
