package com.loopers.application.payment;

import com.loopers.domain.order.*;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
@Transactional
@DisplayName("PaymentUseCase 통합 테스트")
class PaymentUseCaseIntegrationTest {

    @Autowired private PaymentUseCase paymentUseCase;
    @Autowired private OrderService orderService;
    @Autowired private ProductService productService;
    @Autowired private ProductRepository productRepository;
    @Autowired private PointService pointService;
    @Autowired private PaymentRepository paymentRepository;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 100L;
    private static final int ORDER_QTY = 2;
    private static final int STOCK_QTY = 10;
    private static final int PRICE = 500;

    /*@Nested
    @DisplayName("결제 성공 시")
    class SuccessCase {

        @Test
        @DisplayName("재고 차감 + 포인트 사용 + 결제 완료 처리까지 성공한다")
        void pay_success() {
            // Arrange
            Product product = Product.create(PRODUCT_ID, "상품", PRICE, STOCK_QTY);
            productRepository.save(product);

            Order order = orderService.create(USER_ID, "order-001");
            order.addLine(OrderLine.create(PRODUCT_ID, ORDER_QTY, product.getPrice()));

            int paymentAmount = product.getPrice() * ORDER_QTY;
            order.calculatePaymentAmount(paymentAmount);

            pointService.charge(USER_ID, 10_000); // 포인트 충분히 충전

            PaymentInfo.Pay payInfo = new PaymentInfo.Pay(order.getId(), "CARD");

            // Act
            PaymentResult.Pay result = paymentUseCase.pay(USER_ID, payInfo);

            // Assert
            assertThat(result.isPaymentConfirmed()).isTrue();
            assertThat(result.paymentId()).isNotNull();

            Product updated = productRepository.findById(PRODUCT_ID).orElseThrow();
            assertThat(updated.getStockQuantity()).isEqualTo(STOCK_QTY - ORDER_QTY);
        }
    }

    @Nested
    @DisplayName("결제 실패 시")
    class FailCase {

        @Test
        @DisplayName("재고가 부족하면 예외가 발생하고 결제 실패 처리된다")
        void pay_fail_due_to_stock() {
            // Arrange
            Product product = Product.create(PRODUCT_ID, "상품", PRICE, 1); // 재고 부족
            productRepository.save(product);

            Order order = orderService.create(USER_ID, "order-002");
            order.addLine(OrderLine.create(PRODUCT_ID, ORDER_QTY, product.getPrice()));
            order.calculatePaymentAmount(product.getPrice() * ORDER_QTY);

            pointService.charge(USER_ID, 10_000);

            PaymentInfo.Pay payInfo = new PaymentInfo.Pay(order.getId(), "CARD");

            // Act & Assert
            assertThatThrownBy(() -> paymentUseCase.pay(USER_ID, payInfo))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("재고가 부족");

            Product updated = productRepository.findById(PRODUCT_ID).orElseThrow();
            assertThat(updated.getStockQuantity()).isEqualTo(1); // 차감되지 않음
        }

        @Test
        @DisplayName("포인트 부족 시 예외가 발생하고 결제 실패 처리된다")
        void pay_fail_due_to_point() {
            // Arrange
            Product product = Product.create(PRODUCT_ID, "상품", PRICE, STOCK_QTY);
            productRepository.save(product);

            Order order = orderService.create(USER_ID, "order-003");
            order.addLine(OrderLine.create(PRODUCT_ID, ORDER_QTY, product.getPrice()));
            order.calculatePaymentAmount(product.getPrice() * ORDER_QTY);

            pointService.charge(USER_ID, 100); // 포인트 부족

            PaymentInfo.Pay payInfo = new PaymentInfo.Pay(order.getId(), "CARD");

            // Act & Assert
            assertThatThrownBy(() -> paymentUseCase.pay(USER_ID, payInfo))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("포인트 결제에 실패");

            Product updated = productRepository.findById(PRODUCT_ID).orElseThrow();
            assertThat(updated.getStockQuantity()).isEqualTo(STOCK_QTY); // 차감되지 않음
        }
    }*/
}
