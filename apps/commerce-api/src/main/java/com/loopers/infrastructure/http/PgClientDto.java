package com.loopers.infrastructure.http;


import java.util.List;

public class PgClientDto {

    public record PgAuthRequest(
            String orderId,
            String cardType,
            String cardNo,
            String amount,
            String callbackUrl
    ) {
    }

    public record PgAuthResponse(
            Meta meta,
            Data data
    ) {
        public record Meta(
                String result,
                String errorCode,
                String message
        ) {
        }

        public record Data(
                String transactionKey,
                String status,
                String reason
        ) {
        }
    }

    public record PgTxDetailResponse(
            Meta meta,
            Data data
    ) {
        public record Meta(
                String result,
                String errorCode,
                String message
        ) {
        }

        public record Data(
                String transactionKey,
                String orderId,
                String cardType,
                String cardNo,
                int amount,
                String status,
                String reason
        ) {
        }
    }

    public record PgTxListResponse(
            Meta meta,
            Data data
    ) {
        public record Meta(
                String result,
                String errorCode,
                String message
        ) {
        }

        public record Data(
                String orderId,
                List<Transaction> transactions
        ) {
            public record Transaction(
                    String transactionKey,
                    String status,
                    String reason
            ) {
            }
        }
    }
}
