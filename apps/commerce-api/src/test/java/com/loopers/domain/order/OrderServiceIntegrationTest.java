package com.loopers.domain.order;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.commonvo.Quantity;
import com.loopers.infrastructure.order.OrderJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService sut;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @MockitoSpyBean
    private OrderJpaRepository orderRepository;

    private final Long USER_ID = 1L;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("createOrderByRequestId() 호출 시")
    class CreateOrder {

        @Test
        @DisplayName("같은 orderRequestId가 없으면, 주문을 생성한다")
        void createNewOrder() {
            // Arrange
            String orderRequestId = UUID.randomUUID().toString();
            Optional<Order> order = orderRepository.findByOrderRequestId(orderRequestId);
            assertThat(order).isEmpty();

            // Act
            OrderQuery.CreatedOrder actual = sut.createOrderByRequestId(USER_ID, orderRequestId);

            // Assert
            assertAll(
                    () -> assertThat(actual).isNotNull(),
                    () -> assertThat(actual.isNewlyCreated()).isTrue(),
                    () -> assertThat(actual.order().getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(actual.order().getOrderRequestId()).isEqualTo(orderRequestId)
            );
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("같은 orderRequestId가 존재하면, 기존 주문을 반환한다")
        void returnExistingOrder() {
            // Arrange
            String orderRequestId = UUID.randomUUID().toString();
            Order saved = orderRepository.save(Order.create(USER_ID, orderRequestId));
            reset(orderRepository);

            // Act
            OrderQuery.CreatedOrder actual = sut.createOrderByRequestId(USER_ID, orderRequestId);

            // Assert
            assertAll(
                    () -> assertThat(actual.isNewlyCreated()).isFalse(),
                    () -> assertThat(actual.order().getId()).isEqualTo(saved.getId())
            );
            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getUserOrder() 호출 시")
    class GetUserOrder {

        @Test
        @DisplayName("userId가 일치하면, 주문을 반환한다")
        void returnOrder_whenUserMatches() {
            // Arrange
            Order order = orderRepository.save(Order.create(USER_ID, UUID.randomUUID().toString()));

            // Act
            Order actual = sut.getUserOrder(USER_ID, order.getId());

            // Assert
            assertThat(actual.getId()).isEqualTo(order.getId());
        }

        @Test
        @DisplayName("userId가 일치하지 않으면, FORBIDDEN 예외가 발생한다.")
        void throwsException_whenUserMismatch() {
            // Arrange
            Order order = orderRepository.save(Order.create(USER_ID + 1, UUID.randomUUID().toString()));

            // Act & Assert
            CoreException exception = assertThrows(CoreException.class, () ->
                    sut.getUserOrder(USER_ID, order.getId())
            );
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("finalizePaymentResult() 호출 시")
    class FinalizePayment {

        @Test
        @DisplayName("결제 성공이면, 상태가 PAID로 변경된다")
        void markPaid() {
            // Arrange
            Order order = orderRepository.save(Order.create(USER_ID, UUID.randomUUID().toString()));

            // Act
            boolean isPaymentConfirmed = true;
            sut.finalizeOrderResult(order, isPaymentConfirmed);

            // Assert
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("결제 실패이면, 상태가 PAID_FAILED로 변경된다")
        void markFailed() {
            // Arrange
            Order order = orderRepository.save(Order.create(USER_ID, UUID.randomUUID().toString()));

            // Act
            boolean isPaymentConfirmed = false;
            sut.finalizeOrderResult(order, isPaymentConfirmed);

            // Assert
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID_FAILED);
        }
    }

    @DisplayName("calculateOrderAmountByAddLines() 호출시, ")
    @Nested
    @Transactional
    class CalculateOrderAmount {
        @DisplayName("주문 금액 계산 시, 주문에 OrderLine과 주문 총 금액이 저장된다.")
        @Test
        void calculateOrderAmountByAddLines_success() {
            // Arrange
            Order order = orderRepository.save(Order.create(1L, "req-1"));
            OrderLine line1 = OrderLine.create(101L, Quantity.of(2), Money.of(1000L)); // 2000
            OrderLine line2 = OrderLine.create(102L, Quantity.of(1), Money.of(2000L)); // 2000
            OrderLine line3 = OrderLine.create(103L, Quantity.of(3), Money.of(500L));  // 1500
            List<OrderLine> orderLines = List.of(line1, line2, line3);

            // Act
            Money totalAmount = sut.calculateOrderAmountByAddLines(order, orderLines);

            // Assert
            Order actual = orderRepository.findById(order.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(actual.getOrderLines()).hasSize(3),
                    () -> assertThat(totalAmount.getAmount()).isEqualTo(5500L),
                    () -> assertThat(actual.getTotalAmount()).isEqualTo(Money.of(5500L))
            );
        }
    }

    @DisplayName("calculatePaymentAmount() 호출 시, ")
    @Nested
    @Transactional
    class CalculatePaymentAmount {
        @DisplayName("결제 금액 계산 시, 할인 금액과 결제 금액이 저장된다.")
        @Test
        void calculatePaymentAmount_success() {
            // Arrange
            Order order = orderRepository.save(Order.create(1L, "req-2"));
            OrderLine line1 = OrderLine.create(101L, Quantity.of(2), Money.of(1000L)); // 2000
            order.addOrderLine(List.of(line1));
            order.calculateOrderAmount();
            Money totalAmount = order.getTotalAmount();
            Money discountAmount = Money.of(100L);

            // Act
            Money paymentAmount = sut.calculatePaymentAmount(order, discountAmount);

            // Assert
            Order actual = orderRepository.findById(order.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(actual.getTotalAmount()).isEqualTo(totalAmount),
                    () -> assertThat(paymentAmount).isEqualTo(totalAmount.subtract(discountAmount)),
                    () -> assertThat(actual.getPaymentAmount()).isEqualTo(totalAmount.subtract(discountAmount))
            );
        }
    }
}
