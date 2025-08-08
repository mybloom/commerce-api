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

    @DisplayName("하나의 상품에 여러 명이 동시에 좋아요 요청 시, 좋아요 수는 정확히 1씩 증가하고 요청 성공/실패가 구분되어야 한다.")
    @Test
    void testLikeCountConcurrentRequestsWithSingleLikePerUser() throws Exception {
        // assign
        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        List<CompletableFuture<Boolean>> tasks = new ArrayList<>(threadCount);

        AtomicInteger exceptionCount = new AtomicInteger(0);
        AtomicInteger duplicatedCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);

        // act
        for (int i = 0; i < threadCount; i++) {
            final long userId = i + 1;
            tasks.add(
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            LikeResult.LikeRegisterResult result = likeUseCase.register(userId, productId);
                            if (result.isDuplicatedRequest()) {
                                duplicatedCount.incrementAndGet();
                                return false;
                            } else {
                                successCount.incrementAndGet();
                                return true;
                            }
                        } catch (Exception e) {
                            exceptionCount.incrementAndGet();
                            return false;
                        }
                    }, executorService)
            );
        }

        // assert
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        int totalSuccess = successCount.get();
        int totalDuplicated = duplicatedCount.get();
        int totalException = exceptionCount.get();

        Product product = productRepository.findById(productId).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(totalException)
                    .as("예외 발생 횟수 검증")
                    .isEqualTo(0); // 예외는 없어야 함

            softly.assertThat(totalDuplicated)
                    .as("중복 요청 수 검증")
                    .isEqualTo(0); // 유저가 다 다르므로 중복 요청 없어야 함

            softly.assertThat(totalSuccess)
                    .as("성공한 요청 수 검증")
                    .isEqualTo(threadCount);

            softly.assertThat(product.getLikeCount().getValue())
                    .as("최종 likeCount는 요청 수와 같아야 함")
                    .isEqualTo(threadCount);
        });
    }
}

