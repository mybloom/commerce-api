package com.loopers.domain.order;


import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;

class OrderLineServiceTest {

    private final OrderLineService sut = new OrderLineService();

    private Product createProduct(Long id, long price) {
        Product product = Product.from("상품", price, ProductStatus.AVAILABLE, 0, Quantity.of(10), LocalDate.now(), 1L);
        // 테스트용으로 ID 세팅 (JPA 없이 단위 테스트라면 강제 가능)
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }


    @Test
    @DisplayName("상품 목록에 없는 ID가 포함되면 예외가 발생한다")
    void createOrderLines_fail_dueToMissingProduct() {
        // given
        Product product = createProduct(1L, 1000L);
        List<Product> products = List.of(product);

        List<OrderLineCommand> lines = List.of(
                new OrderLineCommand(1L, Quantity.of(2)),
                new OrderLineCommand(2L, Quantity.of(1)) // 2L는 없음
        );

        // when & then
        CoreException exception = assertThrows(CoreException.class,
                () -> sut.createOrderLines(lines, products));

        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    @DisplayName("상품 개수가 주문 라인보다 많으면 예외가 발생한다")
    void createOrderLines_fail_dueToExtraProducts() {
        // given
        Product product1 = createProduct(1L, 1000L);
        Product product2 = createProduct(2L, 2000L);
        Product extra = createProduct(3L, 3000L);

        List<Product> products = List.of(product1, product2, extra); // 3개
        List<OrderLineCommand> lines = List.of(
                new OrderLineCommand(1L, Quantity.of(1)),
                new OrderLineCommand(2L, Quantity.of(1))
        ); // 2개

        // when & then
        CoreException exception = assertThrows(CoreException.class,
                () -> sut.createOrderLines(lines, products));

        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(exception.getMessage()).contains("일치하지 않습니다");
    }

    @Test
    @DisplayName("상품 개수가 주문 라인보다 적으면 예외가 발생한다")
    void createOrderLines_fail_dueToMissingProducts() {
        // given
        Product product = createProduct(1L, 1000L);
        List<Product> products = List.of(product); // 1개

        List<OrderLineCommand> lines = List.of(
                new OrderLineCommand(1L, Quantity.of(1)),
                new OrderLineCommand(2L, Quantity.of(1)) // 2L는 없음
        ); // 2개

        // when & then
        CoreException exception = assertThrows(CoreException.class,
                () -> sut.createOrderLines(lines, products));

        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    @DisplayName("상품 ID가 모두 일치하고 개수도 같으면 OrderLine을 정상 생성한다")
    void createOrderLines_success() {
        // given
        Product product1 = createProduct(1L, 1000L);
        Product product2 = createProduct(2L, 2000L);

        List<Product> products = List.of(product1, product2);
        List<OrderLineCommand> lines = List.of(
                new OrderLineCommand(1L, Quantity.of(2)),
                new OrderLineCommand(2L, Quantity.of(1))
        );

        // when
        List<OrderLine> orderLines = sut.createOrderLines(lines, products);

        // then
        assertThat(orderLines).hasSize(2);
        assertThat(orderLines).extracting(OrderLine::getProductId).containsExactly(1L, 2L);
    }
}
