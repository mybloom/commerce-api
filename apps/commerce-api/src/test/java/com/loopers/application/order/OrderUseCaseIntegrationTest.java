package com.loopers.application.order;


import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.BrandStatus;
import com.loopers.domain.commonvo.Money;
import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.coupon.*;
import com.loopers.domain.order.*;
import com.loopers.domain.order.Order;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
class OrderUseCaseIntegrationTest {

    @Autowired
    private OrderUseCase sut;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @MockitoSpyBean
    private OrderRepository orderRepository;
    @MockitoSpyBean
    private ProductRepository productRepository;
    @MockitoSpyBean
    private BrandRepository brandRepository;
    @MockitoSpyBean
    private PointRepository pointRepository;
    @MockitoSpyBean
    private UserCouponRepository userCouponRepository;
    @MockitoSpyBean
    private CouponRepository couponRepository;

    private static final Long USER_ID = 1L;
    private static final String ORDER_REQUEST_ID = "order-123";

    private Long productId1;
    private Long productId2;
    private Money userBalance;

    @BeforeEach
    void setUp() {
        Brand brand = brandRepository.save(Brand.from("나이키", "글로벌 브랜드", BrandStatus.ACTIVE));

        Product product1 = Product.from("상품1", 1000L, ProductStatus.AVAILABLE, 0, Quantity.of(10), LocalDate.now().minusDays(1), brand.getId());
        Product product2 = Product.from("상품2", 2000L, ProductStatus.AVAILABLE, 0, Quantity.of(10), LocalDate.now().minusDays(1), brand.getId());

        productRepository.save(product1);
        productRepository.save(product2);
        productId1 = product1.getId();
        productId2 = product2.getId();

        Point point = pointRepository.save(Point.create(USER_ID, Money.of(10_000L)));
        userBalance = point.balance();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("주문 등록 시,")
    @Nested
    class OrderCreate {

        @DisplayName("상품번호가 올바르고 보유 포인트가 충분하면, Order와 OrderLine이 저장된다.")
        @Test
        void placeOrder_successfullyPersistsOrderLinesAndCalculatesTotalAmount() {
            // assign
      /*      List<OrderInfoOld.ItemInfo> items = List.of(
                    new OrderInfoOld.ItemInfo(productId1, 2), // 2 * 1000 = 2000
                    new OrderInfoOld.ItemInfo(productId2, 3)  // 3 * 2000 = 6000
            );*/
            List<OrderInfo.Create.Product> items = List.of(
                    OrderInfo.Create.Product.builder()
                            .productId(productId1)
                            .quantity(2)
                            .build(), // 2 * 1000 = 2000
                    OrderInfo.Create.Product.builder()
                            .productId(productId2)
                            .quantity(3)
                            .build()  // 3 * 2000 = 6000
            );

            // act
            List<Long> userCouponIds = Collections.emptyList();
//            OrderResult.OrderRequestResult result = sut.order(USER_ID, ORDER_REQUEST_ID, items, userCouponIds);
            OrderInfo.Create orderInfo = OrderInfo.Create.of(USER_ID, ORDER_REQUEST_ID, items, userCouponIds);
            OrderResult.OrderRequestResult result = sut.order(orderInfo);

            // assert
            Order order = orderRepository.findByIdWithOrderLines(result.getOrderId()).orElseThrow();
            List<OrderLine> orderLines = order.getOrderLines();

            assertAll(
                    () -> assertThat(orderLines).hasSize(2),
                    () -> assertThat(orderLines).extracting(OrderLine::getProductId)
                            .containsExactlyInAnyOrder(productId1, productId2),
                    () -> assertThat(order.calculateOrderAmount()).isEqualTo(Money.of(8000L))
            );
        }

        /*
        @Test
        @DisplayName("쿠폰이 적용되면, Order에 할인 금액이 반영된다.")
        void placeOrder_appliesCouponDiscount() {
            // arrange
            List<OrderInfoOld.ItemInfo> items = List.of(
                    new OrderInfoOld.ItemInfo(productId1, 2) // 2 * 1000 = 2000
            );

            // 쿠폰 생성 (10% 할인)
            DiscountPolicy discountPolicy = DiscountPolicy.of(DiscountType.RATE, new BigDecimal("0.100"));
            Coupon coupon = couponRepository.save(
                    Coupon.create("10% 할인 쿠폰", discountPolicy, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))
            );

            Money originalOrderAmount = productRepository.findById(productId1).orElseThrow()
                    .getPrice().multiply(Quantity.of(2));
            assertThat(originalOrderAmount).isEqualTo(Money.of(2000L)); // 사전 검증

            // 유저 쿠폰 발급
            UserCoupon userCoupon = userCouponRepository.save(UserCoupon.create(USER_ID, coupon));
            List<Long> userCouponIds = List.of(userCoupon.getId());

            // act
            OrderResult.OrderRequestResult result = sut.order(USER_ID, ORDER_REQUEST_ID, items, userCouponIds);

            // assert
            Order order = orderRepository.findByIdWithOrderLines(result.orderId()).orElseThrow();
            Money expectedTotal = Money.of(2000L);
            Money expectedDiscount = Money.of(200L);  // 10% of 2000
            Money expectedPayment = Money.of(1800L);  // 2000 - 200

            assertAll(
                    () -> assertThat(order.getTotalAmount()).isEqualTo(expectedTotal),
                    () -> assertThat(order.getDiscountAmount()).isEqualTo(expectedDiscount),
                    () -> assertThat(order.getPaymentAmount()).isEqualTo(expectedPayment),
                    () -> assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING)
            );
        }

        @Test
        @DisplayName("정액 할인 쿠폰이 적용되면, Order에 할인 금액이 반영된다.")
        void placeOrder_appliesFixedAmountCouponDiscount() {
            // arrange
            List<OrderInfoOld.ItemInfo> items = List.of(
                    new OrderInfoOld.ItemInfo(productId1, 2) // 2 * 1000 = 2000
            );

            // 정액 쿠폰 생성 (1,500원 할인)
            DiscountPolicy discountPolicy = DiscountPolicy.of(DiscountType.FIXED_AMOUNT, new BigDecimal("1500"));
            Coupon coupon = couponRepository.save(
                    Coupon.create("1,500원 할인 쿠폰", discountPolicy, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))
            );

            Money originalOrderAmount = productRepository.findById(productId1).orElseThrow()
                    .getPrice().multiply(Quantity.of(2));
            assertThat(originalOrderAmount).isEqualTo(Money.of(2000L)); // 사전 검증

            // 유저 쿠폰 발급
            UserCoupon userCoupon = userCouponRepository.save(UserCoupon.create(USER_ID, coupon));
            List<Long> userCouponIds = List.of(userCoupon.getId());

            // act
            OrderResult.OrderRequestResult result = sut.order(USER_ID, ORDER_REQUEST_ID, items, userCouponIds);

            // assert
            Order order = orderRepository.findByIdWithOrderLines(result.orderId()).orElseThrow();
            Money expectedTotal = Money.of(2000L);
            Money expectedDiscount = Money.of(1500L);
            Money expectedPayment = Money.of(500L); // 2000 - 1500

            assertAll(
                    () -> assertThat(order.getTotalAmount()).isEqualTo(expectedTotal),
                    () -> assertThat(order.getDiscountAmount()).isEqualTo(expectedDiscount),
                    () -> assertThat(order.getPaymentAmount()).isEqualTo(expectedPayment),
                    () -> assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING)
            );
        }

        @Test
        @DisplayName("기간이 만료된 쿠폰을 사용하면 주문은 실패해야 한다.")
        void placeOrder_shouldFailIfCouponExpired() {
            // arrange
            List<OrderInfoOld.ItemInfo> items = List.of(
                    new OrderInfoOld.ItemInfo(productId1, 2) // 2000원
            );

            // 만료된 쿠폰 생성 (endAt가 과거)
            DiscountPolicy discountPolicy = DiscountPolicy.of(DiscountType.FIXED_AMOUNT, new BigDecimal("1000"));
            Coupon expiredCoupon = couponRepository.save(
                    Coupon.create("만료 쿠폰", discountPolicy, LocalDate.now().minusDays(10), LocalDate.now().minusDays(5))
            );

            // 유저 쿠폰 발급
            UserCoupon userCoupon = userCouponRepository.save(UserCoupon.create(USER_ID, expiredCoupon));
            List<Long> userCouponIds = List.of(userCoupon.getId());

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                sut.order(USER_ID, ORDER_REQUEST_ID, items, userCouponIds);
            });

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.CONFLICT);

            Order order = orderRepository.findByOrderRequestId(ORDER_REQUEST_ID).orElse(null);
            assertThat(order).isNotNull();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.VALIDATION_FAILED);
        }

        @Test
        @DisplayName("이미 사용된 쿠폰을 사용하면 주문은 실패해야 한다.")
        void placeOrder_shouldFailIfCouponAlreadyUsed() {
            // arrange
            List<OrderInfoOld.ItemInfo> items = List.of(
                    new OrderInfoOld.ItemInfo(productId1, 2) // 2000원
            );

            // 유효한 쿠폰 생성
            DiscountPolicy discountPolicy = DiscountPolicy.of(DiscountType.FIXED_AMOUNT, new BigDecimal("1000"));
            Coupon validCoupon = couponRepository.save(
                    Coupon.create("1,000원 쿠폰", discountPolicy, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))
            );

            // 유저 쿠폰 생성 및 사용 처리
            UserCoupon userCoupon = userCouponRepository.save(UserCoupon.create(USER_ID, validCoupon));
            userCoupon.markUsed(); // 사용 처리
            userCoupon = userCouponRepository.save(userCoupon);
            assertThat(userCoupon.isUsed()).isTrue();

             List<Long> userCouponIds = List.of(userCoupon.getId());

            // act & assert
             CoreException exception = assertThrows(CoreException.class, () -> {
                sut.order(USER_ID, ORDER_REQUEST_ID, items, userCouponIds);
            });

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.CONFLICT);

            Order order = orderRepository.findByOrderRequestId(ORDER_REQUEST_ID).orElse(null);
            assertThat(order).isNotNull();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.VALIDATION_FAILED);
        }*/

    }
}
