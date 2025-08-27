package com.loopers.interfaces.api.payment;


import com.loopers.application.payment.dto.PaymentCallbackInfo;

public class PaymentCallbackDto {

    public record ProcessRequest(
            String orderId,
            Long amount,
            String transactionKey,
            String status,
            String reason
    ) {
        public PaymentCallbackInfo.ProcessTransaction convertToCommand() {
            return new PaymentCallbackInfo.ProcessTransaction(
                    this.orderId,
                    this.amount,
                    this.transactionKey,
                    this.status,
                    this.reason
            );
        }
    }
}
