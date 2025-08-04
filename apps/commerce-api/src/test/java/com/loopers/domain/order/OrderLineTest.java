package com.loopers.domain.order;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.commonvo.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class OrderLineTest {

    private static final Long PRODUCT_ID = 100L;
    private static final Quantity QUANTITY = Quantity.of(3);
    private static final Money PRICE = Money.of(2000L);
    private static final Money EXPECTED_SUB_TOTAL = Money.of(6000L);

    @DisplayName("OrderLine 생성 시,")
    @Nested
    class Create {

        @Test
        @DisplayName("상품Id, 수량, 가격을 전달하면, OrderLine 객체가 생성된다.")
        void createOrderLine_successfully() {
            // Act
            OrderLine orderLine = new OrderLine(PRODUCT_ID, QUANTITY, PRICE);

            // Assert
            assertThat(orderLine).isNotNull();
            assertThat(orderLine.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(orderLine.getQuantity()).isEqualTo(QUANTITY);
            assertThat(orderLine.getPrice()).isEqualTo(PRICE);
            assertThat(orderLine.getCreatedAt()).isNull(); // JPA persist 전이므로 null
        }
    }

    @DisplayName("각 상품별 총액 계산 시,")
    @Nested
    class SubTotal {

        @Test
        @DisplayName("상품ID마다 수량 * 가격 결과가 반환된다.")
        void returnsSubtotalCorrectly() {
            // Arrange
            OrderLine orderLine = new OrderLine(PRODUCT_ID, QUANTITY, PRICE);

            // Act
            Money subtotal = orderLine.getSubTotal();

            // Assert
            assertThat(subtotal).isEqualTo(EXPECTED_SUB_TOTAL);
        }
    }
}
