package com.loopers.domain.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.BrandStatus;
import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
class LikeServiceIntegrationTest {


    @Autowired
    private LikeService sut;

    @MockitoSpyBean
    private LikeHistoryRepository likeHistoryRepository;

    @MockitoSpyBean
    private UserRepository userRepository;

    @MockitoSpyBean
    private BrandRepository brandRepository;

    @MockitoSpyBean
    private ProductRepository productRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;


    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("좋아요 등록을 요청할 때,")
    @Nested
    class register {

        private Long userId;
        private Long productId;

        @BeforeEach
        void setUp() {
            //사용자와 상품을 저장
            User user = userRepository.save(new User("member123", "test@example.com", "2000-01-01", Gender.MALE));
            Brand brand = brandRepository.save(
                Brand.from("Nike", "Global Sports Brand", BrandStatus.ACTIVE)
            );
            Product product = productRepository.save(
                Product.from(
                    "Test Product",
                    1000L,
                    ProductStatus.AVAILABLE,
                    0,
                    Quantity.of(100),
                    LocalDate.now(),
                    brand.getId()
                )
            );
            userId = user.getId();
            productId = product.getId();
        }

        @Test
        @DisplayName("이미 좋아요한 경우, 중복요청은 참을 포함해 LikeHistory 도메인을 응답한다.")
        void returnsLikeHistory_whenAlreadyExists() {
            // Arrange
            LikeHistory saved = likeHistoryRepository.save(LikeHistory.from(userId, productId));
            Optional<LikeHistory> found = likeHistoryRepository.findById(saved.getId());
            assertThat(found).isPresent();

            // Act
            LikeQuery.LikeRegisterQuery actual = sut.register(userId, productId);

            // Assert
            assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.isDuplicatedRequest()).isTrue(),
                () -> assertThat(actual.likeHistory().getUserId()).isEqualTo(userId),
                () -> assertThat(actual.likeHistory().getProductId()).isEqualTo(productId),
                () -> assertThat(actual.likeHistory().getId()).isNotNull()
            );
        }

        @Test
        @DisplayName("좋아요 기록이 없으면, 중복요청 아님을 포함해 LikeHistory를 응답한다.")
        void returnsIsDuplicatedFalse_whenNoLikeHistory() {
            // Arrange
            Optional<LikeHistory> found = likeHistoryRepository.findByUserIdAndProductId(userId, productId);
            assertThat(found).isEmpty();

            // Act
            LikeQuery.LikeRegisterQuery actual = sut.register(userId, productId);

            // Assert
            assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.isDuplicatedRequest()).isFalse(),
                () -> assertThat(actual.likeHistory().getUserId()).isEqualTo(userId),
                () -> assertThat(actual.likeHistory().getProductId()).isEqualTo(productId),
                () -> assertThat(actual.likeHistory().getId()).isNotNull()
            );
        }
    }

    @DisplayName("좋아요 해제를 요청할 때,")
    @Nested
    class remove {

        private Long userId;
        private Long productId;

        @BeforeEach
        void setUp() {
            //사용자와 상품을 저장
            User user = userRepository.save(new User("member123", "test@example.com", "2000-01-01", Gender.MALE));
            Brand brand = brandRepository.save(
                Brand.from("Nike", "Global Sports Brand", BrandStatus.ACTIVE)
            );
            Product product = productRepository.save(
                Product.from(
                    "Test Product",
                    1000L,
                    ProductStatus.AVAILABLE,
                    0,
                    Quantity.of(100),
                    LocalDate.now(),
                    brand.getId()
                )
            );
            userId = user.getId();
            productId = product.getId();
        }

        @Test
        @DisplayName("좋아요 이력이 없으면, 중복 요청으로 처리된다.")
        void returnsDuplicate_whenNoLikeHistoryExists() {
            // Arrange
            Optional<LikeHistory> found = likeHistoryRepository.findByUserIdAndProductId(userId, productId);
            assertThat(found).isEmpty();

            // Act
            LikeQuery.LikeRemoveQuery actual = sut.remove(userId, productId);

            // Assert
            assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.isDuplicatedRequest()).isTrue(),
                () -> assertThat(actual.userId()).isEqualTo(userId),
                () -> assertThat(actual.productId()).isEqualTo(productId)
            );
        }

        @Test
        @DisplayName("좋아요 이력이 있고 삭제되지 않았으면, 성공적으로 삭제된다.")
        void removesLikeHistory_whenExistsAndNotDeleted() {
            // Arrange
            LikeHistory saved = likeHistoryRepository.save(LikeHistory.from(userId, productId));
            assertThat(saved.isDeleted()).isFalse();

            // Act
            LikeQuery.LikeRemoveQuery actual = sut.remove(userId, productId);

            // Assert
            LikeHistory updated = likeHistoryRepository.findById(saved.getId()).orElseThrow();

            assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.isDuplicatedRequest()).isFalse(),
                () -> assertThat(actual.userId()).isEqualTo(userId),
                () -> assertThat(actual.productId()).isEqualTo(productId),
                () -> assertThat(updated.isDeleted()).isTrue()
            );
        }

        @Test
        @DisplayName("이미 삭제된 좋아요 이력이면, 중복 요청으로 처리된다.")
        void returnsDuplicate_whenAlreadyDeleted() {
            // Arrange
            LikeHistory saved = likeHistoryRepository.save(LikeHistory.from(userId, productId));
            saved.delete();
            likeHistoryRepository.save(saved);

            // Act
            LikeQuery.LikeRemoveQuery result = sut.remove(userId, productId);

            // Assert
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.isDuplicatedRequest()).isTrue(),
                () -> assertThat(result.userId()).isEqualTo(userId),
                () -> assertThat(result.productId()).isEqualTo(productId)
            );
        }
    }

    @DisplayName("좋아요 기록을 조회할 때,")
    @Nested
    class retrieveHistories {

        private Long likeHaveUserId;
        private Long likeNoHaveUserId;
        private Long brandId;
        private List<Product> savedProducts = new ArrayList<>();

        @BeforeEach
        void setUp() {
            // 사용자 저장
            User user = userRepository.save(new User("member1", "test1@example.com", "2000-01-01", Gender.MALE));
            likeHaveUserId = user.getId();
            likeNoHaveUserId = userRepository.save(new User("member2", "test2@example.com", "2000-01-01", Gender.MALE))
                .getId();

            // 브랜드 저장
            Brand brand = brandRepository.save(
                Brand.from("Nike", "Global Sports Brand", BrandStatus.ACTIVE)
            );
            brandId = brand.getId();

            // 상품 5개 저장
            for (int i = 0; i < 5; i++) {
                Product product = Product.from(
                    "Test Product " + i,
                    1000L + i,
                    ProductStatus.AVAILABLE,
                    0,
                    Quantity.of(100),
                    LocalDate.now().minusDays(i),
                    brandId
                );
                Product savedProduct = productRepository.save(product);
                savedProducts.add(savedProduct);
            }
        }

        @Test
        @DisplayName("좋아요 기록이 없으면, 빈 페이지를 반환한다.")
        void returnsEmptyPage_whenNoLikeHistories() {
            // Act
            Page<LikeHistory> result = sut.retrieveHistories(likeNoHaveUserId, Pageable.unpaged());

            // Assert
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getContent()).isEmpty(),
                () -> assertThat(result.getTotalElements()).isEqualTo(0)
            );
        }

        @Test
        @DisplayName("좋아요 기록이 존재하면, 해당 페이지를 반환한다.")
        void returnsPageWithHistories_whenLikeHistoriesExist() {
            // Arrange
            int likeCount = 1;
            likeHistoryRepository.save(LikeHistory.from(likeHaveUserId, savedProducts.get(0).getId()));

            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<LikeHistory> actual = sut.retrieveHistories(likeHaveUserId, pageable);

            // Assert
            assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.getContent()).isNotEmpty(),
                () -> assertThat(actual.getTotalElements()).isEqualTo(likeCount),
                () -> assertThat(actual.getContent())
                    .allMatch(like -> like.getUserId().equals(likeHaveUserId))
            );

            verify(likeHistoryRepository, times(1))
                .findByUserIdAndDeletedAtIsNull(likeHaveUserId, pageable);
        }


    }
}
