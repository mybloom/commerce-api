package com.loopers.application.payment;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductRepository;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
@Sql(scripts = "/sql/order-concurrent-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class PaymentUseCaseConcurrencyTest {
    private static final Logger log = LoggerFactory.getLogger(PaymentUseCaseConcurrencyTest.class);

    @Autowired
    private PaymentUseCase paymentUseCase;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final int THREAD_COUNT = 20;
    private static final Long USER_ID = 1L;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("1명의 사용자가 서로 다른 주문 20건을 동시에 결제 시도하면, 포인트와 재고가 정확히 차감된다")
    @Test
    void concurrentPaymentWithDifferentOrders() throws InterruptedException, ExecutionException {
        //arrange 검증
        Money beforeUserBalance = pointRepository.findByUserId(USER_ID).orElseThrow()
                .balance();
        Long totalPaymentAmount = orderRepository.findTotalPaymentAmountByUserId(USER_ID);
        assertThat(beforeUserBalance.getAmount()).isEqualTo(100_000);
        assertThat(totalPaymentAmount).isEqualTo(20_000);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            final long orderId = 1000L + i; // 1001 ~ 1020
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    PaymentInfo.Pay payInfo = PaymentInfo.Pay.of(USER_ID, orderId, PaymentMethod.POINT.name());
                    paymentUseCase.pay(payInfo);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }, executorService);
            futures.add(future);
        }

        List<Boolean> results = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList())
                .get();

        long successCount = results.stream().filter(b -> b).count();
        long failureCount = results.stream().filter(b -> !b).count();

        Point actual = pointRepository.findByUserId(USER_ID).orElseThrow();
        Quantity stockQuantity = productRepository.findById(1L).orElse(null).getStockQuantity();

        assertSoftly(softly -> {
            softly.assertThat(successCount)
                    .as("결제 성공 건수")
                    .isEqualTo(THREAD_COUNT);

            softly.assertThat(actual.balance())
                    .as("포인트 차감 후 잔액")
                    .isEqualTo(beforeUserBalance.subtract(Money.of(totalPaymentAmount)));


            softly.assertThat(stockQuantity.getAmount())
                    .as("재고 차감 수량")
                    .isEqualTo(1000 - 20);
        });

        log.info("✅ 결제 성공 수: " + successCount);
        log.info("❌ 결제 실패 수: " + failureCount);
        log.info("💰 남은 포인트: " + actual.balance().getAmount());
        log.info("📦 남은 재고 수량: " + stockQuantity.getAmount());
    }
}
