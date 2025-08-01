package com.loopers.application.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.BrandStatus;
import com.loopers.domain.like.LikeHistory;
import com.loopers.domain.like.LikeHistoryRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
class LikeUseCaseIntegrationTest {

    @Autowired
    private LikeUseCase sut;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @MockitoSpyBean
    private UserRepository userRepository;
    @MockitoSpyBean
    private ProductRepository productRepository;
    @MockitoSpyBean
    private BrandRepository brandRepository;
    @MockitoSpyBean
    private LikeHistoryRepository likeHistoryRepository;

    private Long userId;
    private Long productId;

    @BeforeEach
    void setUp() {
        // 유저 저장
        User user = userRepository.save(new User("member123", "test@example.com", "1990-01-01", Gender.MALE));

        // 브랜드 & 상품 저장
        Brand brand = brandRepository.save(Brand.from("Nike", "Global brand", BrandStatus.ACTIVE));
        Product product = productRepository.save(
                Product.from(
                        "테스트 상품",
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

    @DisplayName("좋아요를 등록할 때,")
    @Nested
    class register {
        @Test
        @DisplayName("처음 좋아요를 등록하면, 중복요청은 거짓이고, 상품 likeCount가 1 증가한다")
        void register_firstTimeLike_increasesLikeCount() {
            Optional<LikeHistory> foundLikeHistory = likeHistoryRepository.findByUserIdAndProductId(userId, productId);
            assertThat(foundLikeHistory).isEmpty();
            Product foundProduct = productRepository.findById(productId).orElseThrow();
            int beforeLikeCount = 0;
            assertThat(foundProduct.getLikeCount().getValue()).isEqualTo(beforeLikeCount);

            LikeResult.LikeRegisterResult actual = sut.register(userId, productId);

            // Assert
            Product updated = productRepository.findById(productId).orElseThrow();

            assertAll(
                    () -> assertThat(actual.isDuplicatedRequest()).isFalse(),
                    () -> assertThat(updated.getLikeCount().getValue()).isEqualTo(beforeLikeCount + 1)
            );
        }

        @Test
        @DisplayName("이미 좋아요한 경우 중복요청은 참이고, likeCount는 증가하지 않는다")
        void register_duplicateLike_doesNotIncreaseLikeCount() {
            // Arrange
            sut.register(userId, productId);
            Optional<LikeHistory> foundLikeHistory = likeHistoryRepository.findByUserIdAndProductId(userId, productId);
            Optional<Product> foundProduct = productRepository.findById(productId);
            assertThat(foundLikeHistory).isPresent();
            int beforeLikeCount = 1;
            assertThat(foundProduct.get().getLikeCount().getValue()).isEqualTo(beforeLikeCount);

            // Act
            LikeResult.LikeRegisterResult result = sut.register(userId, productId);

            // Assert
            Product updated = productRepository.findById(productId).orElseThrow();

            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.isDuplicatedRequest()).isTrue(),
                    () -> assertThat(updated.getLikeCount().getValue()).isEqualTo(beforeLikeCount)
            );
        }
    }
}

