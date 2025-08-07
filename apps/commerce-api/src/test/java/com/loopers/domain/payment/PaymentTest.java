package com.loopers.domain.payment;

import com.loopers.domain.commonvo.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;

class PaymentTest {

    private static final Long ORDER_ID = 1L;
    private static final Money AMOUNT = Money.of(10_000L);
    private static final PaymentMethod PAYMENT_METHOD = PaymentMethod.POINT;
    private static final PaymentStatus PAYMENT_STATUS_CONFIRMED = PaymentStatus.CONFIRMED;
    private static final PaymentStatus PAYMENT_STATUS_CANCELED = PaymentStatus.CANCELED;

    @DisplayName("결제 객체 생성 시,")
    @Nested
    class Confirm {

        @Test
        @DisplayName("결제 정보를 전달하면, Payment 객체가 정상적으로 생성된다.")
        void createPayment_successfully() {
            // Act
            Payment payment = Payment.confirm(ORDER_ID, PAYMENT_METHOD, AMOUNT, PaymentStatus.CONFIRMED);

            // Assert
            assertAll(
                    () -> assertThat(payment.getOrderId()).isEqualTo(ORDER_ID),
                    () -> assertThat(payment.getAmount()).isEqualTo(AMOUNT)
            );
        }

        //사실상 Money.of(-1000L) 에서 예외 발생함. 테스트코드 주석처리
  /*      @Test
        @DisplayName("금액이 0 미만일 경우 예외가 발생한다")
        void throwException_whenAmountIsNegative() {
            // Act
            CoreException exception = assertThrows(CoreException.class, () ->
                    Payment.confirm(ORDER_ID, PAYMENT_METHOD, Money.of(-1000L), PAYMENT_STATUS_CANCELED)
            );

            // Assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }*/

        @Test
        @DisplayName("orderId, method, amount, status 중 하나라도 null이면 예외가 발생한다")
        void throwException_whenRequiredFieldIsNull() {
            // Arrange
            Long validOrderId = 1L;
            PaymentMethod validMethod = PaymentMethod.POINT;
            Money validAmount = Money.of(1000L);
            PaymentStatus validStatus = PaymentStatus.CONFIRMED;

            // Act & Assert
            assertAll(
                    () -> {
                        Long nullOrderId = null;
                        assertThrows(CoreException.class, () ->
                                Payment.confirm(nullOrderId, validMethod, validAmount, validStatus)
                        );
                    },
                    () -> {
                        PaymentMethod nullPaymentMethod = null;
                        assertThrows(CoreException.class, () ->
                                Payment.confirm(validOrderId, nullPaymentMethod, validAmount, validStatus)
                        );
                    },
//                    () -> {
//                        Money nullAmount = null;
//                        assertThrows(CoreException.class, () ->
//                                Payment.confirm(validOrderId, validMethod, nullAmount, validStatus)
//                        );
//                    },
                    () -> {
                        PaymentStatus nullPaymentStatus = null;
                        assertThrows(CoreException.class, () ->
                                Payment.confirm(validOrderId, validMethod, validAmount, nullPaymentStatus)
                        );
                    }
            );
        }

    }
}
