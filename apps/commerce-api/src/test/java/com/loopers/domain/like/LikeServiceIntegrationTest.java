package com.loopers.domain.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.BrandStatus;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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
                100L,
                LocalDate.now(),
                brand.getId()
            )
        );
        userId = user.getId();
        productId = product.getId();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("좋아요 등록을 요청할 때,")
    @Nested
    class register {

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


}
