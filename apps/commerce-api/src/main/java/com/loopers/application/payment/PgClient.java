package com.loopers.application.payment;

/**
 * PG(결제대행사)와의 연동 포트(Port).
 * - 도메인/유스케이스는 이 인터페이스만 의존하고,
 * - 실제 통신은 인프라 어댑터(Feign/WebClient 등)에서 구현합니다.
 */
@FunctionalInterface
public interface PgClient {

    /**
     * 카드 결제 인가(authorization) 요청을 생성합니다.
     * 성공 시 보통 3DS/리다이렉트 등의 추가 절차를 위해 Pending 상태로 진입합니다.
     */
    PgAuthResponse authorize(PgAuthRequest request);


    /**
     * PG 인가 요청 DTO.
     */
    record PgAuthRequest(
            String orderId,     // 상점 측 주문 ID
            long amount,        // 결제 금액)
            String cardType,   // 삼성, 신한...등 카드사 코드
            String cardNumber, // 카드 번호(암호화/마스킹 필요)
            String callbackUrl // 결제 완료 후PG가 콜백할 URL
    ) {
    }


    /**
     * PG 인가 응답 DTO.
     **/
    record PgAuthResponse(
            Meta meta,
            Data data
    ) {
        public record Meta(
                String result,     // 요청 수립 성공 여부 : "SUCCESS" | "FAIL" (가정)
                String errorCode,  // 실패/거절 코드 (없을 수 있음)
                String message     // 메시지 (없을 수 있음)
        ) {
        }

        public record Data(
                String transactionKey, // PG 트랜잭션 키
                String status,
                String reason          // 거절/실패 사유 코드(없을 수 있음)
        ) {
        }
    }
}
