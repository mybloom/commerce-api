package com.loopers.application.like;

import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.like.LikeHistoryRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
class LikeUseCaseConcurrentTest {

    @Autowired
    private LikeUseCase likeUseCase;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LikeHistoryRepository likeHistoryRepository;

    private static final int THREAD_COUNT = 20;
    private static Long productId;

    @BeforeAll
    static void setUpOnce(@Autowired ProductRepository productRepository,
                          @Autowired LikeHistoryRepository likeHistoryRepository) {
        likeHistoryRepository.deleteAll();

        Long brandId = 1L;
        Product product = Product.from(
                "상품1", 1000L, ProductStatus.AVAILABLE,
                0, Quantity.of(10), LocalDate.now().minusDays(1), brandId);
        productRepository.save(product);
        productId = product.getId();
    }

    @Test
    @DisplayName("동시 요청 시 성공/실패 요청을 기록하고 likeCount 검증")
    void likeCountConcurrencyTest_withSuccessFailLogging() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        AtomicInteger duplicatedCount = new AtomicInteger();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final long userId = i + 1; // 100명 유저
            executorService.submit(() -> {
                try {
                    LikeResult.LikeRegisterResult result = likeUseCase.register(userId, productId);

                    if (result.isDuplicatedRequest()) {
                        duplicatedCount.incrementAndGet();
                        System.out.println("[중복] userId = " + userId);
                        failCount.incrementAndGet();
                    } else {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.err.println("[예외 발생] userId = " + userId + ", message = " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        Product product = productRepository.findById(productId).orElseThrow();

        // 결과 출력
        System.out.println("🟢 성공한 요청 수: " + successCount.get());
        System.out.println("🔴 실패한 요청 수: " + failCount.get());
        System.out.println("🎯 최종 likeCount: " + product.getLikeCount().getValue());
        System.out.println("🔁 중복 요청 수: " + duplicatedCount.get());

        // 검증
        assertThat(product.getLikeCount().getValue()).isEqualTo(successCount.get());
        assertThat(successCount.get() + failCount.get()).isEqualTo(THREAD_COUNT);
    }



}

