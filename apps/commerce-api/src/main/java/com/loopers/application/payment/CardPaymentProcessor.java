package com.loopers.application.payment;

import com.loopers.application.payment.dto.PaymentFailureInfo;
import com.loopers.application.payment.dto.PaymentInfo;
import com.loopers.application.payment.dto.PaymentResult;
import com.loopers.domain.order.Order;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.pg.PaymentGateway;
import com.loopers.domain.payment.pg.PgDto;
import com.loopers.domain.sharedkernel.PaymentEvent;
import com.loopers.support.error.pg.PgGatewayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;


@Slf4j
@Component
@RequiredArgsConstructor
public class CardPaymentProcessor implements PaymentProcessor {

    private final PaymentGateway paymentGateway;
    private final PaymentService paymentService;
    private final PaymentFailureHandler failureHandler;
    private final ApplicationEventPublisher eventPublisher;

    private final static String callbackUrl = "http://localhost:8080/api/v1/payments/pg/callback";
    private final static String storeId = "store123"; //ecommerce pg용 상점 아이디

    //todo: final PaymentInfo.Pay info 이거 받는 부분 거슬림. info를 바로 쓸 수도 있음으로..
    @Override
    public PaymentResult.Pay process(final PaymentInfo.Pay info, final Order order) {
        final PaymentInfo.CardPay carPayInfo = (PaymentInfo.CardPay) info;

        // PG 요청 생성
        PgDto.AuthCommand authCommand = PgDto.AuthCommand.of(
                storeId,
                carPayInfo.getOrderId(),
                carPayInfo.getCardType(),
                carPayInfo.getCardNumber(),
                order.getPaymentAmount(),
                buildReturnUrl(carPayInfo.getOrderId()) //todo: pathParam 안써서 안해도 됨
        );

        // 1) PG 호출
        PgDto.AuthQuery pgAuthQuery = null;
        try {
            pgAuthQuery = paymentGateway.auth(authCommand);
        } catch (PgGatewayException e) {
            log.error("PgGatewayException: [{}] {} ({})", e.getResult(), e.getErrorCode(), e.getMessage());

            failureHandler.handleFailedCardPayment(
                    PaymentFailureInfo.Fail.of(
                            info, order.getPaymentAmount(), e.getResult() + ":" + e.getMessage()
                    )
            );
            throw e;
        }

        // 2) 결제 정보 저장
        Payment payment = paymentService.createCardPayment(carPayInfo.convertToCommand(order.getPaymentAmount(), pgAuthQuery.transactionKey()));

        // 결제 요청 데이터 전송 이벤트
        PaymentEvent.PaymentInitiated event = new PaymentEvent.PaymentInitiated(payment.getId(), info.getOrderId());
        eventPublisher.publishEvent(event);

        return PaymentResult.Pay.of(payment.getId(), payment.getStatus().name(), info.getOrderId());
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
