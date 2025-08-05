package com.loopers.domain.order;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.loopers.domain.order.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    private static final Long USER_ID = 1L;
    private static final Long PAYMENT_ID = 500L;
    private static final Money PAYMENT_AMOUNT = Money.of(6000L);
    private static final String ORDER_REQUEST_ID = UUID.randomUUID().toString();

    private static final Long PRODUCT_ID = 1L;
    private static final Quantity QUANTITY = Quantity.of(2);
    private static final Long PRODUCT_PRICE = 1000L;

    private List<OrderLineCommand> orderLineCommands;
    private List<Product> products;
    private Money expectedTotal = Money.ZERO;

    @BeforeEach
    void setUp() {
        Product product = Product.from(
                "상품1",
                PRODUCT_PRICE,
                ProductStatus.AVAILABLE,
                10,
                100L,
                LocalDate.now().minusDays(1),
                1L
        );
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID); // ID 설정

        products = List.of(product);
        orderLineCommands = List.of(new OrderLineCommand(PRODUCT_ID, QUANTITY));

        expectedTotal = product.getPrice().multiply(QUANTITY);
    }

    @DisplayName("주문 객체 생성 시")
    @Nested
    class Create {

        @Test
        @DisplayName("처음 주문을 생성하면, 상태는 PENDING이고 기본값들이 설정된다")
        void createOrder_successfully() {
            // Act
            Order order = Order.create(USER_ID, ORDER_REQUEST_ID);

            // Assert
            assertThat(order).isNotNull();
            assertThat(order.getUserId()).isEqualTo(USER_ID);
            assertThat(order.getStatus()).isEqualTo(PENDING);
            assertThat(order.getOrderRequestId()).isEqualTo(ORDER_REQUEST_ID);
            assertThat(order.getOrderLines()).isEmpty();
        }
    }

    @DisplayName("주문 객체 수정 시,")
    @Nested
    class AddProduct {

        @Test
        @DisplayName("선택한 상품목록과 각 수량은, orderLines에 반영된다")
        void addProductToOrder() {
            // Arrange
            Order order = Order.create(USER_ID, ORDER_REQUEST_ID);
            OrderLineService orderLineService = new OrderLineService();
            List<OrderLine> orderLines = orderLineService.createOrderLines(orderLineCommands, products);

            // Act
            order.addOrderLine(orderLines);

            // Assert
            assertThat(order.getOrderLines()).hasSize(products.size());
            assertThat(order.getOrderLines().get(0).getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(order.getOrderLines().get(0).getQuantity()).isEqualTo(QUANTITY);
            assertThat(order.getOrderLines().get(0).getPrice()).isEqualTo(Money.of(PRODUCT_PRICE));
        }
    }

    @DisplayName("주문 금액 계산 시,")
    @Nested
    class CalculateTotal {

        @Test
        @DisplayName("상품들이 추가된 후, calculateTotal()을 호출하면 총합이 계산된다")
        void calculateTotalAmount() {
            // Arrange
            Order order = Order.create(USER_ID, ORDER_REQUEST_ID);
            OrderLineService orderLineService = new OrderLineService();
            List<OrderLine> orderLines = orderLineService.createOrderLines(orderLineCommands, products);
            order.addOrderLine(orderLines);

            // Act
            Money actual = order.calculateOrderAmount();

            // Assert
            assertThat(actual).isEqualTo(expectedTotal);
        }
    }

    @DisplayName("결제 상태 변경 시,")
    @Nested
    class Status {

        @Test
        @DisplayName("결제 정상 처리가 되면, 상태는 PAID로 변경되고 결제 ID와 금액이 설정된다")
        void markAsPaid() {
            // Arrange
            Order order = Order.create(USER_ID, ORDER_REQUEST_ID);

            // Act
            order.markPaid(PAYMENT_ID, PAYMENT_AMOUNT);

            // Assert
            assertThat(order.getStatus()).isEqualTo(PAID);
            assertThat(order.getPaymentId()).isEqualTo(PAYMENT_ID);
            assertThat(order.getPaymentAmount()).isEqualTo(PAYMENT_AMOUNT);
        }

        @Test
        @DisplayName("결제가 오류가 나면, 주문 상태는 PAID_FAILED로 설정된다")
        void markAsFailed() {
            // Arrange
            Order order = Order.create(USER_ID, ORDER_REQUEST_ID);

            // Act
            order.markFailed();

            // Assert
            assertThat(order.getStatus()).isEqualTo(PAID_FAILED);
        }
    }
}
