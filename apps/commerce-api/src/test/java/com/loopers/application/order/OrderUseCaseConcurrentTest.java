package com.loopers.application.order;

import com.loopers.domain.coupon.UserCouponRepository;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductRepository;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
@Sql(scripts = "/sql/coupon-concurrent-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CouponUseCaseConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(CouponUseCaseConcurrencyTest.class);

    @Autowired
    private OrderUseCase orderUseCase;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final int THREAD_COUNT = 50;
    private static final Long USER_ID = 1L;
    private static final String ORDER_REQUEST_PREFIX = "order-coupon-";

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("동시에 동일한 쿠폰 사용 시, 단 1명만 성공하고 나머지는 실패한다")
    void onlyOneShouldSucceedWhenCouponUsedConcurrently() throws Exception {
        // Arrange
        Long couponId = 1L; // 미리 발급된 user_coupon.id
        Long productId = 1L;
        int quantity = 1;

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final String orderRequestId = ORDER_REQUEST_PREFIX + i;
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    OrderInfo.ItemInfo item = new OrderInfo.ItemInfo(productId, quantity);
                    List<Long> couponIds = List.of(couponId);
                    orderUseCase.order(USER_ID, orderRequestId, List.of(item), couponIds);
                    return true;
                } catch (Exception e) {
//                    log.warn("❌ 실패: {}", e.getMessage());
                    return false;
                }
            }, executor));
        }

        List<Boolean> results = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList())
                .get();

        long successCount = results.stream().filter(Boolean::booleanValue).count();
        long failureCount = THREAD_COUNT - successCount;

        // Assert
        assertSoftly(softly -> {
            softly.assertThat(successCount)
                    .as("쿠폰 성공 사용 건수")
                    .isEqualTo(1);

            softly.assertThat(failureCount)
                    .as("쿠폰 실패 사용 건수")
                    .isEqualTo(THREAD_COUNT - 1);

            softly.assertThat(userCouponRepository.findById(couponId).orElseThrow().isUsed())
                    .as("쿠폰 사용 여부")
                    .isTrue();
        });

        log.info("✅ 성공: {}", successCount);
        log.info("❌ 실패: {}", failureCount);
    }
}

