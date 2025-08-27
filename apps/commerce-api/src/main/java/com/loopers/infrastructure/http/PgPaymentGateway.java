package com.loopers.infrastructure.http;

import com.loopers.domain.payment.pg.PgDto;
import com.loopers.domain.payment.pg.PaymentGateway;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PgPaymentGateway implements PaymentGateway {

    private final PgFeignClient pgFeignClient;
    private final PgErrorTranslator pgErrorTranslator;

    @Override
    public PgDto.AuthQuery auth(final PgDto.AuthCommand command) {
        final PgClientDto.PgAuthRequest request = PgClientDto.PgAuthRequest.from(command);

        PgClientDto.PgAuthResponse response = null;
        try {
            response = pgFeignClient.requestPayment(command.storeId(), request);
        } catch (FeignException e) {
            // Feign 예외 스택트레이스 남기고, 도메인 예외로 변환
            log.error("PG 요청 실패: {}", e.getMessage(), e);
            throw pgErrorTranslator.translate(e);
        }

        return response.convertToPgAuthQuery();
    }
}
