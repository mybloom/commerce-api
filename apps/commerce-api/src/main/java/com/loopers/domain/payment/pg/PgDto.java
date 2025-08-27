package com.loopers.domain.payment.pg;

import com.loopers.domain.commonvo.Money;

public class PgDto {
    public record AuthCommand(
            String storeId,
            String orderId,
            String cardType,
            String cardNo,
            String amount,
            String callbackUrl
    ) {
        public static AuthCommand of(
                String storeId, Long orderId, String cardType, String cardNo, Money amount, String callbackUrl) {
            return new AuthCommand(
                    storeId,
                    String.format("%010d", orderId),
                    cardType,
                    cardNo,
                    amount.getAmount().toString(),
                    callbackUrl
            );
        }
    }

    public record AuthQuery(
            String result,
            String errorCode,
            String message,
            String transactionKey,
            String status,
            String reason
    ) {
        //어댑터가 꺼낸 원시값을 받아 조립
        public static AuthQuery fromRaw(
                String metaResult, String errorCode, String message,
                String transactionKey, String status, String reason
        ) {
            return new AuthQuery(
                    metaResult,
                    errorCode,
                    message,
                    transactionKey,
                    status,
                    reason
            );
        }

    }
}
