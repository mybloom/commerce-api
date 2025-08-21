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

    private static final OrderLineService orderLineService = new OrderLineService();
    private static final Long USER_ID = 1L;
    private static final String ORDER_REQUEST_ID = "order-123";

    private Long productId1;
    private Long productId2;
    private Long orderId;
    private Money userBalance;
    private final List<Product> products = new ArrayList<>();
    private Point point;

    @BeforeEach
    void setUp() {
        setupProducts();
        setupUserPoint();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private void setupProducts() {
        Long brandId = 1L;
        Product product1 = Product.from("상품1", 1000L, ProductStatus.AVAILABLE, 0, Quantity.of(10), LocalDate.now().minusDays(1), brandId);
        Product product2 = Product.from("상품2", 2000L, ProductStatus.AVAILABLE, 0, Quantity.of(10), LocalDate.now().minusDays(1), brandId);

        productRepository.save(product1);
        productRepository.save(product2);

        productId1 = product1.getId();
        productId2 = product2.getId();

        products.add(product1);
        products.add(product2);
    }

    private void setupUserPoint() {
        //todo: 밑에 달아둔 주석이 영속성 컨텍스트에 의해 이후 값이 변경될 수 있다는 것이 아닐 수 있는 것 같다. 실패해서 검증 시 새로 조회했다.
        point = pointRepository.save(Point.create(USER_ID, Money.of(10_000L))); //주의: 영속성 컨텍스트에 의해 이후 값이 변경될 수 있음
        userBalance = point.balance(); // 주의: 조회 시점의 스냅샷 값. 이후 balance 변경 시 반영되지 않음
    }

    private void prepareOrderAndOrderLines(Quantity q1, Quantity q2) {
        Order order = Order.create(USER_ID, ORDER_REQUEST_ID);
        List<OrderLineCommand> commands = List.of(
                new OrderLineCommand(productId1, q1),
                new OrderLineCommand(productId2, q2)
        );
        List<OrderLine> orderLines = orderLineService.createOrderLines(commands, products);
        order.addOrderLine(orderLines);
        order.calculateOrderAmount();
        order.applyDiscount(Money.ZERO);
        orderRepository.save(order);
        orderId = order.getId();
    }

    @Test
    @DisplayName("결제 성공 시, 결제 성공 이력이 저장된다")
    void pay_success() {
        prepareOrderAndOrderLines(Quantity.of(2), Quantity.of(2));

        PaymentInfo.Pay payInfo = PaymentInfo.Pay.of(USER_ID, orderId, PaymentMethod.POINT.name());
        Order order = orderRepository.findByIdWithOrderLines(orderId).orElseThrow();
        Money expectedAmount = order.getPaymentAmount();

        PaymentResult.Pay result = sut.pay(payInfo);
        Payment payment = paymentRepository.findById(result.getPaymentId()).orElseThrow();

        assertAll(
                () -> assertThat(result.getPaymentId()).isNotNull(),
                () -> assertThat(payment.getFailureReason()).isNull(),
                () -> assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED),
                () -> assertThat(payment.getAmount()).isEqualTo(expectedAmount)
        );
    }

    @Test
    @DisplayName("하나라도 재고 부족 시, 전체 재고 차감 없이 주문/결제 실패 이력이 저장되고 CONFLICT 예외 발생")
    void pay_fail_due_to_stock() {
        prepareOrderAndOrderLines(Quantity.of(3), Quantity.of(20));

        Product before1 = productRepository.findById(productId1).orElseThrow();
        Product before2 = productRepository.findById(productId2).orElseThrow();

        CoreException ex = assertThrows(CoreException.class,
                () -> sut.pay(PaymentInfo.Pay.of(USER_ID, orderId, PaymentMethod.POINT.name())));

        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        Product after1 = productRepository.findById(productId1).orElseThrow();
        Product after2 = productRepository.findById(productId2).orElseThrow();

        assertAll(
                () -> assertThat(ex.getErrorType()).isEqualTo(ErrorType.CONFLICT),
                () -> assertThat(ex.getMessage()).contains(PaymentFailureReason.OUT_OF_STOCK.getMessage()),
                () -> assertThat(after1.getStockQuantity()).isEqualTo(before1.getStockQuantity()),
                () -> assertThat(after2.getStockQuantity()).isEqualTo(before2.getStockQuantity()),
                () -> assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED),
                () -> assertThat(payment.getFailureReason()).isEqualTo(PaymentFailureReason.OUT_OF_STOCK)
        );
    }

    @Test
    @DisplayName("재고 충분한 경우, 재고/포인트가 차감되고 주문/결제 성공 이력이 저장된다")
    void paySuccess_whenOrderLinesSufficientStock() {
        Quantity q1 = Quantity.of(2);
        Quantity q2 = Quantity.of(1);
        prepareOrderAndOrderLines(q1, q2);

        Product before1 = productRepository.findById(productId1).orElseThrow();
        Product before2 = productRepository.findById(productId2).orElseThrow();
        Quantity before1Quantity = before1.getStockQuantity();
        Quantity before2Quantity = before2.getStockQuantity();

        Point beforePoint = pointRepository.findByUserId(USER_ID).orElseThrow();
        Money userBalance = beforePoint.balance();

        //act
        PaymentResult.Pay result = sut.pay(PaymentInfo.Pay.of(USER_ID, orderId, PaymentMethod.POINT.name()));

        // assert
        Payment payment = paymentRepository.findById(result.getPaymentId()).orElseThrow();
        Product after1 = productRepository.findById(productId1).orElseThrow();
        Product after2 = productRepository.findById(productId2).orElseThrow();
        Money expectedDeductedAmount = before1.getPrice().multiply(q1)
                .add(before2.getPrice().multiply(q2));
        Point afterPoint = pointRepository.findByUserId(USER_ID).orElseThrow();

        assertAll(
                () -> assertThat(payment.getFailureReason()).isNull(),
                () -> assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CONFIRMED),
                // 재고 확인
                () -> assertThat(after1.getStockQuantity().getAmount()).isEqualTo(before1Quantity.subtract(Quantity.of(2)).getAmount()),
                () -> assertThat(after2.getStockQuantity()).isEqualTo(before2Quantity.subtract(Quantity.of(1))),
                // 포인트 차감 확인
                () -> assertThat(afterPoint.balance().getAmount()).isEqualTo(userBalance.subtract(expectedDeductedAmount).getAmount())
        );
    }

    @Test
    @DisplayName("포인트 부족 시, 포인트와 재고는 차감되지 않고 결제 실패 이력이 저장되고 CONFLICT 예외 발생")
    void pay_fail_due_to_point() {
        prepareOrderAndOrderLines(Quantity.of(3), Quantity.of(10)); // 금액 초과
        //경계값 테스트. 보유량 10개인데, 10개 사용.
        //todo: 에러코드를 남겨둬야 어디서 발생한 에러인지 금방 찾을 것 같다. 수량이 0이 되면 안되게 해놨었다. 현재는 마이너스만 안되게 수정함.

        Order order = orderRepository.findByIdWithOrderLines(orderId).orElseThrow();
        assertThat(userBalance.isLessThan(order.getPaymentAmount())).isTrue();

        CoreException ex = assertThrows(CoreException.class,
                () -> sut.pay(PaymentInfo.Pay.of(USER_ID, orderId, PaymentMethod.POINT.name())));

        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        Product current1 = productRepository.findById(productId1).orElseThrow();
        Product current2 = productRepository.findById(productId2).orElseThrow();
        Point currentPoint = pointRepository.findByUserId(USER_ID).orElseThrow();

        assertAll(
                () -> assertThat(ex.getErrorType()).isEqualTo(ErrorType.CONFLICT),
                () -> assertThat(ex.getMessage()).contains(PaymentFailureReason.INSUFFICIENT_BALANCE.getMessage()),
                () -> assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED),
                () -> assertThat(payment.getFailureReason()).isEqualTo(PaymentFailureReason.INSUFFICIENT_BALANCE),
                () -> assertThat(currentPoint.balance()).isEqualTo(userBalance),
                () -> assertThat(current1.getStockQuantity().getAmount()).isEqualTo(products.get(0).getStockQuantity().getAmount()),
                () -> assertThat(current2.getStockQuantity()).isEqualTo(products.get(1).getStockQuantity())
        );
    }
}

