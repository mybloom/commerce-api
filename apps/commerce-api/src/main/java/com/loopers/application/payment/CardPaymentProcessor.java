package com.loopers.application.payment;

import com.loopers.application.pg.PgGateway;
import com.loopers.domain.commonvo.Money;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentFailureReason;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.infrastructure.http.PgClientDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;


@Slf4j
@Component
@RequiredArgsConstructor
public class CardPaymentProcessor implements PaymentProcessor {

    private final PgGateway pgGateway;

    //    @Value("${payments.card.return-base-url}") //todo
    private String callbackUrl = "http://localhost:8080/api/v1/payments/card/callback";
    private final String storeId = "store123"; //ecommerce pg용 상점 아이디

    @Override
    public PaymentProcessResult process(PaymentInfo.Pay info, Payment payment, Money amount) {
        final PaymentInfo.CardPay carPayInfo = (PaymentInfo.CardPay) info;

        // 1) PG 요청 생성
        PgClientDto.PgAuthRequest pgAuthRequest = new PgClientDto.PgAuthRequest(
                String.format("%010d", carPayInfo.getOrderId()),
                carPayInfo.getCardType(),
                carPayInfo.getCardNumber(),
                amount.getAmount().toString(),
                callbackUrl
        );

        // 2) PG 호출
        PgClientDto.PgAuthResponse pgAuthResponse = null;
        try {
            pgAuthResponse = pgGateway.pgAuthPayment(storeId, pgAuthRequest);
        } catch (FeignException e) {

        }

        // 3) 결과 해석
        if (pgAuthResponse == null) {
            // todo: 재시도 해봐야하는 구간
            log.warn("PG returned null response. orderId={}", info.getOrderId());
            return new PaymentProcessResult.Declined(PaymentFailureReason.PG_COMMUNICATION_ERROR.getMessage());
        }

        if ("SUCCESS".equals(pgAuthResponse.meta().result())) {
            // 카드: 요청 수립 성공 → Pending (콜백에서 최종 승인/실패 처리)
            String txId = pgAuthResponse.data().transactionKey();
            return new PaymentProcessResult.Pending(txId, buildReturnUrl(carPayInfo.getOrderId()));
        }

        // 거절(비재시도) 사유 매핑
        String message = pgAuthResponse.meta().message() + ":" + pgAuthResponse.meta().message();
        return new PaymentProcessResult.Declined(message);
    }

    /**
     * 콜백으로 돌아올 리턴 URL 구성
     */
    private String buildReturnUrl(Long orderId) {
        return UriComponentsBuilder.fromUriString(Objects.requireNonNull(callbackUrl))
                .queryParam("orderId", orderId)
                .build()
                .toUriString();
    }

    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.CARD;
    }
}
