package com.loopers.domain.product;


import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.BrandStatus;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
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
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService sut;

    @MockitoSpyBean
    private ProductRepository productRepository;

    @MockitoSpyBean
    private BrandRepository brandRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품 조회 할 때,")
    @Nested
    class Retrieve {
        private Brand brand;
        private Long brandId;
        private int productTotalCount = 0;

        @BeforeEach
        void setUp() {
            brand = brandRepository.save(
                    Brand.from("Nike", "Global brand", BrandStatus.ACTIVE)
            );
            brandId = brand.getId();
            Brand adidas = brandRepository.save(
                    Brand.from("Adidas", "Global brand!", BrandStatus.ACTIVE)
            );

            // 상품 데이터 여러 개 저장
            for (int i = 1; i <= 5; i++) {
                productRepository.save(
                        Product.from(
                                "Product_nike_" + i,
                                1000L * i,
                                ProductStatus.AVAILABLE,
                                i,
                                100L,
                                LocalDate.now().minusDays(i),
                                brand.getId()
                        )
                );
                productRepository.save(
                        Product.from(
                                "Product_adidas_" + i,
                                1000L * i,
                                ProductStatus.AVAILABLE,
                                i,
                                100L,
                                LocalDate.now().minusDays(i),
                                adidas.getId()
                        )
                );
                productTotalCount += 2;
            }
        }

        @Test
        @DisplayName("브랜드 ID로 상품 목록 조회 시, 리포지토리가 호출되고 결과를 반환한다")
        void retrievesProductListByBrand() {
            // Arrange
            int pageSize = 3;
            Pageable pageable = PageRequest.of(0, pageSize);

            // Act
            Page<ProductListProjection> actual = sut.retrieveListByBrand(brandId, pageable);

            // Assert
            assertAll(
                    () -> assertThat(actual).isNotNull(),
                    () -> assertThat(actual.getContent()).hasSizeLessThanOrEqualTo(pageSize),
                    () -> assertThat(
                            actual.getContent().stream()
                                    .allMatch(p -> p.brandId().equals(brandId))
                    )
            );

            verify(productRepository, times(1))
                    .findAllForListViewByBrand(brandId, pageable);
        }

        @Test
        @DisplayName("상품 목록 조회 시, 리포지토리가 호출되고 결과를 반환한다")
        void retrievesProductList() {
            // Arrange
            int pageSize = 3;
            Pageable pageable = PageRequest.of(0, pageSize);

            // Act
            Page<ProductListProjection> actual = sut.retrieveList(pageable);

            // Assert
            long distinctBrandCount = actual.getContent().stream()
                    .map(ProductListProjection::brandId)
                    .distinct()
                    .count();

            assertAll(
                    () -> assertThat(actual).isNotNull(),
                    () -> assertThat(actual.getContent()).hasSizeLessThanOrEqualTo(pageSize),
                    () -> assertThat(actual.getTotalElements()).isEqualTo(productTotalCount),
                    () -> assertThat(distinctBrandCount).isGreaterThan(1)
            );

            verify(productRepository, times(1))
                    .findAllForListView(pageable);
        }

        @Test
        @DisplayName("존재하지 않은 상품 ID를 전달하면, Optional.empty를 반환한다.")
        void returnEmpty_whenRetrieveOneWithInvalidId() {
            long invalidProductId = -1L;
            Optional<Product> found = productRepository.findById(invalidProductId);
            assertThat(found).isEmpty();

            // Act
            Optional<Product> actual = sut.retrieveOne(invalidProductId);

            // Assert
            assertAll(
                    () -> assertThat(actual).isEmpty()
            );
        }

        @Test
        @DisplayName("존재하는 상품 ID를 전달하면, Product 객체를 반환한다.")
        void retrieveOne() {
            long validProductId = 1L;
            Optional<Product> found = productRepository.findById(validProductId);
            assertThat(found).isPresent();

            // Act
            Optional<Product> actual = sut.retrieveOne(validProductId);

            // Assert
            assertAll(
                    () -> assertThat(actual).isPresent(),
                    () -> assertThat(actual.get().getId()).isEqualTo(validProductId)
            );
        }
    }

    @DisplayName("좋아요 증가/감소 시,")
    @Nested
    class LikeCountUpdate {
        private Product product;

        @BeforeEach
        void setUp() {
            Brand brand = brandRepository.save(
                    Brand.from("Nike", "Popular brand", BrandStatus.ACTIVE)
            );

            product = productRepository.save(
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
        }

        @Test
        @DisplayName("increaseLikeCount를 호출하면, 좋아요 수가 1 증가분이 DB에 반영된다.")
        void increaseLikeCount() {
            // arrange
            int beforeLikeCount = productRepository.findById(product.getId())
                    .orElseThrow()
                    .getLikeCount().getValue();

            // Act
            sut.increaseLikeCount(product);

            // Assert
            Product actual = productRepository.findById(product.getId()).orElseThrow();
            assertThat(actual.getLikeCount().getValue()).isEqualTo(beforeLikeCount + 1);
        }

        @Test
        @DisplayName("decreaseLikeCount를 호출하면, 좋아요 수가 1 감소분이 DB에 반영된다.")
        void decreaseLikeCount() {
            // Arrange
            product.increaseLikeCount(); // 먼저 증가
            productRepository.save(product);
            int beforeLikeCount = product.getLikeCount().getValue();

            // Act
            sut.decreaseLikeCount(product);

            // Assert
            Product actual = productRepository.findById(product.getId()).orElseThrow();
            assertThat(actual.getLikeCount().getValue()).isEqualTo(beforeLikeCount - 1);
        }
    }

}
