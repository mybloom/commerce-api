package com.loopers.application.like;

import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.like.LikeHistoryRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(LikeUseCaseConcurrentTest.class);

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

    @DisplayName("하나의 상품에 각 50명 사용자가 동시에 좋아요 요청 시, 좋아요 수는 1씩 증가하고 중복 요청은 없어야한다.")
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

        log.info("🟢 성공 요청 수: {}", totalSuccess);
        log.info("🔁 중복 요청 수: {}", totalDuplicated);
        log.info("🔴 예외 발생 수: {}", totalException);
        log.info("❤️ 최종 likeCount: {}", product.getLikeCount().getValue());
    }

    @DisplayName("하나의 상품에 대해 50명 사용자가 동시에 좋아요 해제 요청 시, likeCount는 1씩 정확히 감소해야 한다.")
    @Test
    void testLikeCountConcurrentRemoveRequests() throws Exception {
        // given: 50명 유저가 먼저 좋아요 등록
        int userCount = 50;
        for (int i = 0; i < userCount; i++) {
            long userId = i + 1;
            likeUseCase.register(userId, productId);
        }

        Product likedProduct = productRepository.findById(productId).orElseThrow();
        assertThat(likedProduct.getLikeCount().getValue()).isEqualTo(userCount);

        // when: 동시에 좋아요 해제 요청
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        List<CompletableFuture<Boolean>> tasks = new ArrayList<>(userCount);

        AtomicInteger exceptionCount = new AtomicInteger(0);
        AtomicInteger duplicatedCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < userCount; i++) {
            final long userId = i + 1;
            tasks.add(
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            LikeResult.LikeRemoveResult result = likeUseCase.remove(userId, productId);
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

        // then
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        int totalSuccess = successCount.get();
        int totalDuplicated = duplicatedCount.get();
        int totalException = exceptionCount.get();

        Product finalProduct = productRepository.findById(productId).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(totalException)
                    .as("예외 발생 횟수 검증")
                    .isEqualTo(0);

            softly.assertThat(totalDuplicated)
                    .as("중복 요청 수 검증")
                    .isEqualTo(0); // 유저 당 1번 요청이므로 중복 없어야 함

            softly.assertThat(totalSuccess)
                    .as("성공한 해제 요청 수 검증")
                    .isEqualTo(userCount);

            softly.assertThat(finalProduct.getLikeCount().getValue())
                    .as("최종 likeCount는 0이어야 함")
                    .isEqualTo(0);
        });

        log.info("🟢 성공 요청 수: {}", totalSuccess);
        log.info("🔁 중복 요청 수: {}", totalDuplicated);
        log.info("🔴 예외 발생 수: {}", totalException);
        log.info("❤️ 최종 likeCount: {}", finalProduct.getLikeCount().getValue());
    }

}

