package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentFailureReason;

/**
 * 결제수단 Processor가 UseCase로 돌려주는 내부 처리 결과.
 * - Approved : 동기 승인 완료(예: 포인트)
 * - Pending  : 비동기 진행 중(예: 카드, 리다이렉트/콜백 대기)
 * - Declined : 동기 거절/실패
 *
 * sealed interface/record는 JDK 17+가 필요합니다.
 */
public sealed interface PaymentProcessResult {

    /**
     * 동기 승인 완료 (예: 포인트 결제)
     * UseCase에서는 payment_request_success → success 로 종결 처리.
     */
    record Approved(String txId) implements PaymentProcessResult {}

    /**
     * 비동기 진행 중 (예: 카드 결제)
     * UseCase에서는 payment_request_success 까지만 기록하고, 콜백에서 종결.
     */
    record Pending(String txId, String redirectUrl) implements PaymentProcessResult {}

    /**
     * 동기 거절/실패
     * UseCase에서는 payment_request_fail 기록 후 실패 핸들러 처리.
     */
    record Declined(String reason) implements PaymentProcessResult {}
}
