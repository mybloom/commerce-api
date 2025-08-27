package com.loopers.infrastructure.http;


import com.loopers.domain.payment.pg.PgDto;

import java.util.List;

public class PgClientDto {

    public record PgAuthRequest(
            String orderId,
            String cardType,
            String cardNo,
            String amount,
            String callbackUrl
    ) {
        public static PgAuthRequest from(PgDto.AuthCommand command) {
            return new PgAuthRequest(
                    command.orderId(),
                    command.cardType(),
                    command.cardNo(),
                    command.amount(),
                    command.callbackUrl()
            );
        }
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

        public PgDto.AuthQuery convertToPgAuthQuery() {
            String metaResult = this.meta() != null ? this.meta().result() : null;
            String errorCode = this.meta() != null ? this.meta().errorCode() : null;
            String message = this.meta() != null ? this.meta().message() : null;

            String txKey = this.data() != null ? this.data().transactionKey() : null;
            String status = this.data() != null ? this.data().status() : null;
            String reason = this.data() != null ? this.data().reason() : null;

            return PgDto.AuthQuery.fromRaw(metaResult, errorCode, message, txKey, status, reason);
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
