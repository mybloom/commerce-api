package com.loopers.domain.payment.pg;

public interface PaymentGateway {
    PgDto.AuthQuery auth(PgDto.AuthCommand command);
}
