package com.loopers.infrastructure.http;

import com.loopers.application.pg.PgGateway;
import com.loopers.domain.payment.PgQuery;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

//todo: PgClientDto.PgAuthResponse를 바로 보내지 않고, 감싸서 사용.
@Slf4j
@RequiredArgsConstructor
@Component
public class PgFeignGateway implements PgGateway {
    private final PgClient pgClient;

    @CircuitBreaker(name = "pgCircuitBreaker", fallbackMethod = "requestPaymentFallback")
    @Retry(name = "pgRetry", fallbackMethod = "pgAuthRetryFallback")
    @Override
    public PgClientDto.PgAuthResponse pgAuthPayment(String storeId, PgClientDto.PgAuthRequest request) {
        return pgClient.requestPayment(storeId, request);
    }

    @Override
    public PgClientDto.PgTxDetailResponse getTransaction(String storeId, String transactionKey) {
        return pgClient.getTransaction(storeId, transactionKey);
    }

    @Override
    public PgClientDto.PgTxListResponse getPaymentsByOrderId(String storeId, String orderId) {
        return pgClient.getPaymentsByOrderId(storeId, orderId);
    }

    public PgClientDto.PgAuthResponse pgAuthRetryFallback(PgClientDto.PgAuthRequest pgAuthRequest, Throwable t) {
        log.warn("Retry fallback 호출됨 - OrderId: {}, Error: {}",
                pgAuthRequest.orderId(), t.getMessage());

        return new PgClientDto.PgAuthResponse(
                new PgClientDto.PgAuthResponse.Meta(
                        "FAIL",
                        "-9999",
                        "재시도 후 결제에 실패하였습니다: " + t.getMessage()
                ),
                new PgClientDto.PgAuthResponse.Data(
                        null,  // transactionKey 없음
                        "FAIL", // 상태
                        t.getMessage()
                )
        );
    }

}
