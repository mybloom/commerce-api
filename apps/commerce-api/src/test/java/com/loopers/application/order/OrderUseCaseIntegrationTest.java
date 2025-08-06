package com.loopers.application.order;


import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.BrandStatus;
import com.loopers.domain.commonvo.Money;
import com.loopers.domain.order.*;
import com.loopers.domain.order.Order;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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


    private static final Long USER_ID = 1L;
    private static final String ORDER_REQUEST_ID = "order-123";

    private Long productId1;
    private Long productId2;

    @BeforeEach
    void setUp() {
        Brand brand = brandRepository.save(Brand.from("나이키", "글로벌 브랜드", BrandStatus.ACTIVE));

        Product product1 = Product.from("상품1", 1000L, ProductStatus.AVAILABLE, 0, 10L, LocalDate.now().minusDays(1), brand.getId());
        Product product2 = Product.from("상품2", 2000L, ProductStatus.AVAILABLE, 0, 10L, LocalDate.now().minusDays(1), brand.getId());

        productRepository.save(product1);
        productRepository.save(product2);
        productId1 = product1.getId();
        productId2 = product2.getId();

        pointRepository.save(Point.create(USER_ID, Money.of(10_000L)));
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("주문 등록 시,")
    @Nested

    class OrderPlace {
        @DisplayName("상품번호가 올바르고 보유 포인트가 충분하면, Order와 OrderLine이 저장된다.")
        @Test
        void placeOrder_successfullyPersistsOrderLinesAndCalculatesTotalAmount() {
            // assign
            List<OrderInfo.ItemInfo> items = List.of(
                    new OrderInfo.ItemInfo(productId1, 2), // 2 * 1000 = 2000
                    new OrderInfo.ItemInfo(productId2, 3)  // 3 * 2000 = 6000
            );

            // act
            OrderResult.OrderRequestResult result = sut.order(USER_ID, ORDER_REQUEST_ID, items);

            // assert
            Order order = orderRepository.findByIdWithOrderLines(result.orderId()).orElseThrow();
            List<OrderLine> orderLines = order.getOrderLines();

            assertAll(
                    () -> assertThat(orderLines).hasSize(2),
                    () -> assertThat(orderLines).extracting(OrderLine::getProductId)
                            .containsExactlyInAnyOrder(productId1, productId2),
                    () -> assertThat(order.calculateOrderAmount()).isEqualTo(Money.of(8000L))
            );
        }
    }
}
