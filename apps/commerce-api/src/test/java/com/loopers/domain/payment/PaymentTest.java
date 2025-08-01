package com.loopers.domain.payment;

import com.loopers.domain.commonvo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    private static final Long ORDER_ID = 1L;
    private static final Money AMOUNT = Money.of(10_000L);
    private static final Money USED_POINT = Money.of(2_000L);

    @DisplayName("결제 객체 생성 시,")
    @Nested
    class Confirm {

        @Test
        @DisplayName("결제 정보를 전달하면, Payment 객체가 정상적으로 생성된다.")
        void createPayment_successfully() {
            // Act
            Payment payment = new Payment().confirm(ORDER_ID, AMOUNT, USED_POINT);

            // Assert
            assertThat(payment).isNotNull();
            assertThat(payment.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(payment.getAmount()).isEqualTo(AMOUNT);
            assertThat(payment.getUsedPoint()).isEqualTo(USED_POINT);
            assertThat(payment.getCreatedAt()).isNotNull();
        }
    }
}
