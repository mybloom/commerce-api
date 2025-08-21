package com.loopers.domain.payment;

import com.loopers.domain.commonvo.Money;
import com.loopers.infrastructure.payment.PaymentJpaRepository;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
public class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService sut;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @MockitoSpyBean
    private PaymentJpaRepository paymentRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("결제 저장 시,")
    @Nested
    class Save {

        private final Long orderId = 1L;
        private final Money amount = Money.of(1000L);

        @Test
        @DisplayName("정상 결제일 경우, CONFIRMED 상태로 저장된다.")
        void saveConfirmedPayment() {
            // Act
            boolean isPaymentConfirmed = true;
            Long paymentId = sut.saveSuccess(orderId, PaymentMethod.POINT, amount);

            // Assert
            Payment actual = paymentRepository.findById(paymentId).orElseThrow();
            assertAll(
                    () -> assertThat(actual.getOrderId()).isEqualTo(orderId),
                    () -> assertThat(actual.getAmount()).isEqualTo(amount),
                    () -> assertThat(actual.getPaymentMethod()).isEqualTo(PaymentMethod.POINT),
                    () -> assertThat(actual.getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED)
            );

            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository, times(1)).save(paymentCaptor.capture());
            assertThat(paymentCaptor.getValue().getOrderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("잔액부족으로 결제 실패일 경우, CANCELED 상태로 저장되고 실패 사유에 잔액 부족을 기록한다.")
        void saveCanceledPayment() {
            // Act
            boolean isPaymentConfirmed = false;
            Long paymentId = sut.saveFailure(orderId, PaymentMethod.POINT, PaymentFailureReason.INSUFFICIENT_BALANCE);

            // Assert
            Payment actual = paymentRepository.findById(paymentId).orElseThrow();
            assertAll(
                    () -> assertThat(actual.getOrderId()).isEqualTo(orderId),
                    () -> assertThat(actual.getAmount()).isEqualTo(amount),
                    () -> assertThat(actual.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED),
                    () -> assertThat(actual.getFailureReason()).isEqualTo(PaymentFailureReason.INSUFFICIENT_BALANCE)
            );
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }
    }
}
