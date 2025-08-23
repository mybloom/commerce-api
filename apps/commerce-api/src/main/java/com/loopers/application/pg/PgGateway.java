package com.loopers.application.pg;

import com.loopers.infrastructure.http.PgClientDto;

public interface PgGateway {
    PgClientDto.PgAuthResponse pgAuthPayment(String storeId, PgClientDto.PgAuthRequest request);
    PgClientDto.PgTxDetailResponse getTransaction(String storeId, String transactionKey);
    PgClientDto.PgTxListResponse getPaymentsByOrderId(String storeId, String orderId);
}
