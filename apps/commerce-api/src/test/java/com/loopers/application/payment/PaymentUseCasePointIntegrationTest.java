package com.loopers.application.payment;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.order.*;
import com.loopers.domain.order.Order;
import com.loopers.domain.payment.*;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductStatus;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * PaymentUseCase 포인트 결제 통합 테스트
 * - 성공(포인트 충분), 실패(포인트 부족), 실패(재고 부족) 시나리오
 */
@SpringBootTest
@Import(MySqlTestContainersConfig.class)
class PaymentUseCasePointIntegrationTest {

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
    private ProductRepository productRepository;
    @MockitoSpyBean
    private PointRepository pointRepository;
    @MockitoSpyBean
    private OrderRepository orderRepository;
    @MockitoSpyBean
    private PaymentRepository paymentRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final Long USER_ID = 1L;
    private static final String ORDER_REQUEST_ID = "order-123";

    private final List<Product> products = new ArrayList<>();
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        setupProducts();
        // 각 테스트에서 케이스별 포인트/주문 데이터를 별도로 세팅
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private void setupProducts() {
        Long brandId = 1L;
        product1 = Product.from("상품1", 1000L, ProductStatus.AVAILABLE, 0,
                Quantity.of(10), LocalDate.now().minusDays(1), brandId);
        product2 = Product.from("상품2", 2000L, ProductStatus.AVAILABLE, 0,
                Quantity.of(10), LocalDate.now().minusDays(1), brandId);

        productRepository.save(product1);
        productRepository.save(product2);

        Long productId1 = product1.getId();
        Long productId2 = product2.getId();

        products.add(product1);
        products.add(product2);
    }


    /**
     * 사용자 포인트를 특정 금액으로 세팅
     */
    private void seedUserPoint(long balance) {
        Point point = Point.create(USER_ID, Money.of(balance)); // ← 프로젝트 팩토리 메서드에 맞게 수정
        pointRepository.save(point);
    }

    /**
     * 주문 생성 (주문 ID 반환)
     */
    private Long seedOrderForUser(Quantity orderQuantity) {
        // 1) 주문 생성/저장
        Order order = orderRepository.save(Order.create(USER_ID, ORDER_REQUEST_ID));

        // 4) OrderLine 생성
        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(OrderLine.create(product1.getId(), orderQuantity, product1.getPrice()));

        Money orderAmount = orderLines.stream()
                .map(OrderLine::getSubTotal)
                .reduce(Money.ZERO, Money::add);

        Money discountAmount = Money.of(0L);
        Money paymentAmount = orderAmount.subtract(discountAmount);

        // 6) 주문 완료 상태로 세팅 후 저장
        order.complete(orderLines, orderAmount, discountAmount, paymentAmount);
        orderRepository.save(order);

        return order.getId();
    }

    // ===== 시나리오 =====
/*
    @Test
    @DisplayName("포인트가 충분하면 승인(APPROVED)으로 반환된다")
    void pay_point_success_when_balance_is_enough() {
        // Arrange
        Quantity orderQuantity = Quantity.of(1);
        Long orderId = seedOrderForUser(orderQuantity);
        seedUserPoint(*//* balance *//* 10_000L);

        long beforePoint = pointRepository.findByUserId(USER_ID)
                .orElseThrow().balance().getAmount();
        int beforeStock = productRepository.findById(product1.getId())
                .orElseThrow().getStockQuantity().getAmount();

        PaymentInfo.PointPay info = PaymentInfo.PointPay.of(USER_ID, orderId);

        // Act
        PaymentResult.Pay result = sut.pay(info);

        // Assert
        assertThat(result.paymentId()).isNotNull();
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.outcome()).isInstanceOf(PaymentProcessResult.Approved.class);

        PaymentProcessResult.Approved a = (PaymentProcessResult.Approved) result.outcome();
        assertThat(a.txId()).isNotBlank();

        long afterPoint = pointRepository.findByUserId(USER_ID)
                .orElseThrow().balance().getAmount();
        long afterStock = productRepository.findById(product1.getId())
                .orElseThrow().getStockQuantity().getAmount();
        long orderAmount = orderRepository.findByIdWithOrderLines(orderId).orElseThrow()
                .getPaymentAmount().getAmount();
        assertAll(
                // 포인트는 결제 금액만큼 차감되었는지
                () -> assertThat(afterPoint).isEqualTo(beforePoint - orderAmount),
                () -> assertThat(afterPoint).isLessThan(beforePoint),

                // 재고는 주문 수량만큼 차감되었는지(단일 상품 케이스)
                () -> assertThat(afterStock).isEqualTo(beforeStock - orderQuantity.getAmount()),
                () -> assertThat(afterStock).isLessThan(beforeStock)
        );

        Payment payment = paymentRepository.findByOrderIdAndUserId(orderId, USER_ID).orElseThrow();
        assertAll(
                () -> assertThat(payment.getOrderId()).isEqualTo(orderId),
                () -> assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CONFIRMED)
        );
    }

    @Test
    @DisplayName("포인트가 부족하면, CoreException 발생하고 재고가 복구된다, 결제는 FAILED 상태가 된다")
    void pay_point_declined_when_balance_is_insufficient() {
        // Arrange
        Long orderId = seedOrderForUser(Quantity.of(3));
        seedUserPoint(*//* balance *//* 10L); // 부족

        long beforePoint = pointRepository.findByUserId(USER_ID)
                .orElseThrow().balance().getAmount();
        int beforeStock = productRepository.findById(product1.getId())
                .orElseThrow().getStockQuantity().getAmount();

        PaymentInfo.PointPay info = PaymentInfo.PointPay.of(USER_ID, orderId);

        // Act
        CoreException exception = assertThrows(CoreException.class,
                () -> sut.pay(info));

        // Assert
        long afterPoint = pointRepository.findByUserId(USER_ID)
                .orElseThrow().balance().getAmount();
        long afterStock = productRepository.findById(product1.getId())
                .orElseThrow().getStockQuantity().getAmount();

        assertAll(
                () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.CONFLICT),
                () -> assertThat(afterPoint).isEqualTo(beforePoint), // 포인트 복구 검증
                () -> assertThat(afterStock).isEqualTo(beforeStock)  // 재고 복구 검증
        );

        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertAll(
                () -> assertThat(payment.getOrderId()).isEqualTo(orderId),
                () -> assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED)
        );
    }

    @Test
    @DisplayName("재고가 부족하면 거절(PRODUCT_UNAVAILABLE)로 반환되고 결제는 FAILED 상태가 된다")
    void pay_point_declined_when_stock_is_insufficient() {
        // Arrange
        // 상품1 재고 10개인데 20개 주문 → 재고 부족
        Long orderId = seedOrderForUser(Quantity.of(20));
        seedUserPoint(*//* 충분한 포인트 *//* 1_000_000L);

        long beforePoint = pointRepository.findByUserId(USER_ID)
                .orElseThrow().balance().getAmount();
        int beforeStock = productRepository.findById(product1.getId())
                .orElseThrow().getStockQuantity().getAmount();

        PaymentInfo.PointPay info = PaymentInfo.PointPay.of(USER_ID, orderId);

        // Act
        CoreException exception = assertThrows(CoreException.class,
                () -> sut.pay(info));

        // Assert
        long afterPoint = pointRepository.findByUserId(USER_ID)
                .orElseThrow().balance().getAmount();
        long afterStock = productRepository.findById(product1.getId())
                .orElseThrow().getStockQuantity().getAmount();

        assertAll(
                () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.CONFLICT),
                () -> assertThat(afterPoint).isEqualTo(beforePoint), // 포인트 복구 검증
                () -> assertThat(afterStock).isEqualTo(beforeStock)  // 재고 복구 검증
        );

        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertAll(
                () -> assertThat(payment.getOrderId()).isEqualTo(orderId),
                () -> assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED)
        );
    }*/

}
