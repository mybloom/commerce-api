package com.loopers.application.payment;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.order.*;
import com.loopers.domain.payment.*;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
class PaymentUseCaseIntegrationTest {

    @Autowired
    private PaymentUseCase sut;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;
    @Autowired
    private PointService pointService;
    @Autowired
    private PaymentService paymentService;
    @MockitoSpyBean
    private OrderRepository orderRepository;
    @MockitoSpyBean
    private ProductRepository productRepository;
    @MockitoSpyBean
    private PointRepository pointRepository;
    @MockitoSpyBean
    private PaymentRepository paymentRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static OrderLineService orderLineService = new OrderLineService();
    private static final Long USER_ID = 1L;
    private static final String ORDER_REQUEST_ID = "order-123";

    private Long productId1;
    private Long productId2;
    private Money userBalance;
    private Long orderId;
    List<Product> products = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Long brandId = 1L;

        Product product1 = Product.from("상품1", 1000L, ProductStatus.AVAILABLE, 0, Quantity.of(10)
                , LocalDate.now().minusDays(1), brandId);
        Product product2 = Product.from("상품2", 2000L, ProductStatus.AVAILABLE, 0, Quantity.of(10)
                , LocalDate.now().minusDays(1), brandId);
        products.add(product1);
        products.add(product2);

        productRepository.save(product1);
        productRepository.save(product2);
        productId1 = product1.getId();
        productId2 = product2.getId();

        Point point = pointRepository.save(Point.create(USER_ID, Money.of(10_000L)));
        userBalance = point.balance();
    }

    private void prepareOrderAndOrderLines(Quantity product1Quantity, Quantity product2Quantity) {
        Order order = Order.create(USER_ID, ORDER_REQUEST_ID);
        List<OrderLineCommand> orderLineCommands =
                List.of(
                        new OrderLineCommand(productId1, product1Quantity),
                        new OrderLineCommand(productId2, product2Quantity)
                );
        List<OrderLine> orderLines = orderLineService.createOrderLines(orderLineCommands, products);
        order.addOrderLine(orderLines);

        order.calculateOrderAmount();
        order.applyDiscount(Money.ZERO);
        orderRepository.save(order);
        orderId = order.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("결제 성공 시, 결제 성공 이력이 저장된다")
    void pay_success() {
        Quantity product1Quantity = Quantity.of(2);
        Quantity product2Quantity = Quantity.of(2);
        prepareOrderAndOrderLines(product1Quantity, product2Quantity);

        PaymentInfo.Pay payInfo = new PaymentInfo.Pay(orderId, PaymentMethod.POINT);
        Order order = orderRepository.findByIdWithOrderLines(orderId).orElseThrow();
        Money expectedPaymentAmount = order.getPaymentAmount();

        // act
        PaymentResult.Pay result = sut.pay(USER_ID, payInfo);

        // assert
        assertThat(result.paymentId()).isNotNull();

        Payment payment = paymentRepository.findById(result.paymentId()).orElseThrow();
        assertAll(
                () -> assertThat(payment.getFailureReason()).isNull(),
                () -> assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED),
                () -> assertThat(payment.getAmount()).isEqualTo(expectedPaymentAmount)
        );
    }

    @Test
    @DisplayName("하나의 상품이라도 재고 부족 시, 주문 상품들의 모든 재고가 차감이 되지 않고 결제 실패 이력이 저장되고 CONFLICT 예외가 발생한다.")
    void pay_fail_due_to_stock() {
        Quantity product1Quantity = Quantity.of(3);
        Quantity product2Quantity = Quantity.of(20);
        prepareOrderAndOrderLines(product1Quantity, product2Quantity);

        Product beforeProduct1 = productRepository.findById(productId1).orElseThrow();
        Product beforeProduct2 = productRepository.findById(productId2).orElseThrow();
        Quantity product1Stock = beforeProduct1.getStockQuantity();
        Quantity product2Stock = beforeProduct2.getStockQuantity();

        PaymentInfo.Pay payInfo = new PaymentInfo.Pay(orderId, PaymentMethod.POINT);

        // act
        CoreException exception = assertThrows(CoreException.class, ()
                -> sut.pay(USER_ID, payInfo)
        );

        // assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        assertThat(exception.getMessage()).contains(PaymentFailureReason.OUT_OF_STOCK.getMessage());

        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        Product afterProduct1 = productRepository.findById(productId1).orElseThrow();
        Product afterProduct2 = productRepository.findById(productId2).orElseThrow();
        assertAll(
                () -> assertThat(afterProduct1.getStockQuantity().getAmount()).isEqualTo(product1Stock.getAmount()),
                () -> assertThat(afterProduct2.getStockQuantity()).isEqualTo(product2Stock),
                () -> assertThat(payment.getId()).isNotNull(),
                () -> assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED),
                () -> assertThat(payment.getFailureReason()).isEqualTo(PaymentFailureReason.OUT_OF_STOCK)
        );
    }

    @Test
    @DisplayName("모든 상품의 재고가 충분한 경우, 재고가 차감되고 결제 성공 이력이 저장된다.")
    void paySuccess_whenOrderLinesSufficientStock() {
        // Arrange
        Quantity product1Quantity = Quantity.of(2);
        Quantity product2Quantity = Quantity.of(1);
        prepareOrderAndOrderLines(product1Quantity, product2Quantity);

        // 충분한 재고 보장
        Product beforeProduct1 = productRepository.findById(productId1).orElseThrow();
        Product beforeProduct2 = productRepository.findById(productId2).orElseThrow();
        Quantity product1StockBefore = beforeProduct1.getStockQuantity();
        Quantity product2StockBefore = beforeProduct2.getStockQuantity();

        PaymentInfo.Pay payInfo = new PaymentInfo.Pay(orderId, PaymentMethod.POINT);

        // Act
        PaymentResult.Pay result = sut.pay(USER_ID, payInfo);

        // Assert
        Product afterProduct1 = productRepository.findById(productId1).orElseThrow();
        Product afterProduct2 = productRepository.findById(productId2).orElseThrow();
        Payment payment = paymentRepository.findById(result.paymentId()).orElseThrow();

        assertAll(
                () -> assertThat(result.paymentId()).isNotNull(),
                () -> assertThat(afterProduct1.getStockQuantity())
                        .isEqualTo(product1StockBefore.subtract(product1Quantity)),
                () -> assertThat(afterProduct2.getStockQuantity())
                        .isEqualTo(product2StockBefore.subtract(product2Quantity)),
                () -> assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED),
                () -> assertThat(payment.getFailureReason()).isNull()
        );
    }


    @Test
    @DisplayName("포인트 부족 시, 재고, 포인트는 차감되지 않으며 결제 실패 이력이 저장되고 CONFLICT 예외가 발생한다")
    void pay_fail_due_to_point() {
        Quantity product1Quantity = Quantity.of(3);
        Quantity product2Quantity = Quantity.of(10);
        //경계값 테스트. 보유량 10개인데, 10개 사용.
        //todo: 에러코드를 남겨둬야 어디서 발생한 에러인지 금방 찾을 것 같다. 수량이 0이 되면 안되게 해놨었다. 현재는 마이너스만 안되게 수정함.

        prepareOrderAndOrderLines(product1Quantity, product2Quantity);

        // 주문 금액보다 보유 포인트가 작다.
        Order order = orderRepository.findByIdWithOrderLines(orderId).orElseThrow();
        Money paymentAmount = order.getPaymentAmount();
        assertThat(userBalance.isLessThan(paymentAmount)).isTrue();

        PaymentInfo.Pay payInfo = new PaymentInfo.Pay(orderId, PaymentMethod.POINT);

        // act
        CoreException exception = assertThrows(CoreException.class, () -> sut.pay(USER_ID, payInfo));

        // assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        assertThat(exception.getMessage()).contains(PaymentFailureReason.INSUFFICIENT_BALANCE.getMessage());

        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertAll(

                () -> assertThat(payment.getId()).isNotNull(),
                () -> assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED),
                //유저의 포인트량이 동일한지 확인.
                () -> assertThat(userBalance).isEqualTo(pointRepository.findByUserId(USER_ID).orElseThrow().balance()),
                //재고가 동일한지 확인
                () -> assertThat(productRepository.findById(productId1).orElseThrow().getStockQuantity().getAmount())
                        .isEqualTo(products.get(0).getStockQuantity().getAmount()),
                () -> assertThat(productRepository.findById(productId2).orElseThrow().getStockQuantity().getAmount())
                        .isEqualTo(products.get(1).getStockQuantity().getAmount())
        );
    }
}
