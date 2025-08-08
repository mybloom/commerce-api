package com.loopers.domain.product;


import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.BrandStatus;
import com.loopers.domain.commonvo.Quantity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
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
                                Quantity.of(100),
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
                                Quantity.of(100),
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

    @DisplayName("상품 ID 목록으로 상품 조회 시,")
    @Nested
    class GetProductsByIds {
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
                                Quantity.of(100),
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
                                Quantity.of(100),
                                LocalDate.now().minusDays(i),
                                adidas.getId()
                        )
                );
                productTotalCount += 2;
            }
        }

        @Test
        @DisplayName("모든 상품 ID가 유효하면, 상품 리스트를 반환한다.")
        void findAllValidProductsOrThrow_success() {
            // Arrange
            List<Long> validIds = productRepository.findAll().stream()
                    .map(Product::getId)
                    .limit(3)
                    .toList();

            // Act
            List<Product> products = sut.findAllValidProductsOrThrow(validIds);

            // Assert
            assertThat(products).hasSize(validIds.size());
            assertThat(products).extracting(Product::getId).containsAll(validIds);
        }

        @Test
        @DisplayName("상품 ID 중 하나라도 존재하지 않으면, NOT_FOUND  예외가 발생한다.")
        void findAllValidProductsOrThrow_fail_whenAnyMissing() {
            // Arrange
            List<Long> validProductIds = productRepository.findAll().stream()
                    .map(Product::getId)
                    .limit(2)
                    .toList();
            Long invalidProductId = -999L;
            List<Long> mixedIds = List.of(validProductIds.get(0), validProductIds.get(1), invalidProductId);

            // Act
            CoreException exception = assertThrows(CoreException.class,
                    () -> sut.findAllValidProductsOrThrow(mixedIds)
            );

            // Assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
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
                            Quantity.of(100),
                            LocalDate.now(),
                            brand.getId()
                    )
            );
        }

        @Test
        @DisplayName("increaseLikeCount를 호출하면, 좋아요 수가 1 증가분이 DB에 반영된다.")
        @Transactional
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
        @Transactional // todo: 트랜잭션이 없으면, JPA가 영속성 컨텍스트를 플러시하지 않아 변경 사항이 DB에 반영되지 않음. 이 테스트가 과연 필요할까? 이런 테스트는  usecase 단위로 통합테스트코드 작성하는 것이 맞다고 생각함.
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

    @DisplayName("상품 재고 차감 시,")
    @Nested
    @Transactional
    class DeductStock {
        private Brand brand;
        private Long brandId;
        private int productTotalCount = 0;
        private Product product1;
        private Product product2;

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
                product1 = productRepository.save(
                        Product.from(
                                "Product_nike_" + i,
                                1000L * i,
                                ProductStatus.AVAILABLE,
                                i,
                                Quantity.of(100),
                                LocalDate.now().minusDays(i),
                                brand.getId()
                        )
                );
                product2 = productRepository.save(
                        Product.from(
                                "Product_adidas_" + i,
                                1000L * i,
                                ProductStatus.AVAILABLE,
                                i,
                                Quantity.of(100),
                                LocalDate.now().minusDays(i),
                                adidas.getId()
                        )
                );
                productTotalCount += 2;
            }
        }

        @Test
        @DisplayName("재고가 충분할 경우, 재고 차감 요청이 성공적으로 반영된다.")
        void deductStock() {
            // Arrange
            Quantity deductQuantity1 = Quantity.of(5);
            Quantity deductQuantity2 = Quantity.of(3);

            List<ProductCommand.DeductStock> commands = List.of(
                    new ProductCommand.DeductStock(product1, deductQuantity1),
                    new ProductCommand.DeductStock(product2, deductQuantity2)
            );

            Quantity beforeStockOfProduct1 = product1.getStockQuantity();
            Quantity beforeStockOfProduct2 = product2.getStockQuantity();

            // Act
            sut.deductStock(commands);

            // Assert
            Product actualProduct1 = productRepository.findById(product1.getId()).orElseThrow();
            Product actualProduct2 = productRepository.findById(product2.getId()).orElseThrow();

            assertAll(
                    () -> assertThat(actualProduct1.getStockQuantity()).isEqualTo(beforeStockOfProduct1.subtract(deductQuantity1)),
                    () -> assertThat(actualProduct1.getStatus()).isEqualTo(ProductStatus.AVAILABLE),

                    () -> assertThat(actualProduct2.getStockQuantity()).isEqualTo(beforeStockOfProduct2.subtract(deductQuantity2)),
                    () -> assertThat(actualProduct2.getStatus()).isEqualTo(ProductStatus.AVAILABLE)
            );
        }

        @Test
        @DisplayName("재고가 부족할 경우, 상품 상태가 OUT_OF_STOCK으로 변경되고 false를 반환한다.")
        void markSoldOutAndThrowException_whenStockNotEnough() {
            // Arrange
            Quantity overQuantity = Quantity.of(200); // 현재 재고보다 많은 수량
            List<ProductCommand.DeductStock> commands = List.of(
                    new ProductCommand.DeductStock(product1, overQuantity)
            );
            Quantity beforeStockOfProduct1 = product1.getStockQuantity();

            // Act
            boolean isDeductStock = sut.deductStock(commands);

            Product updated = productRepository.findById(product1.getId()).orElseThrow();

            // Assert
            assertAll(
                    () -> assertThat(isDeductStock).isFalse(),
                    () -> assertThat(updated.getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK)
            );
        }
    }
}
