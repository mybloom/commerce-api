package com.loopers.application.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.application.common.PagingCondition;
import com.loopers.application.product.ProductQueryResult.ListViewItemResult;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandStatus;
import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductSortType;
import com.loopers.domain.product.ProductStatus;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.support.paging.PagingPolicy;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
class ProductListUseCaseIntegrationTest {

    private final ProductListUseCase sut;
    private final DatabaseCleanUp databaseCleanUp;

    @MockitoSpyBean
    private ProductJpaRepository productRepository;
    @MockitoSpyBean
    private BrandJpaRepository brandRepository;

    @Autowired
    public ProductListUseCaseIntegrationTest(
            ProductListUseCase productListUseCase,
            DatabaseCleanUp databaseCleanUp
    ) {
        this.sut = productListUseCase;
        this.databaseCleanUp = databaseCleanUp;
    }

    @Autowired
    StringRedisTemplate sredis;

    @BeforeEach
    void flushRedis() {
        sredis.getConnectionFactory().getConnection()
                .serverCommands()
                .flushDb();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("brandId가 존재하는 경우,")
    @Nested
    class WhenBrandIdExists {
        private Brand brand;
        private Long brandId;

        // 기본 설정값
        private static final ProductStatus DEFAULT_PRODUCT_STATUS = ProductStatus.AVAILABLE;
        private static final Long DEFAULT_STOCK_QUANTITY = 10L;

        // 테스트용 값
        // 상품 이름
        private final String productNameOld = "OLD";
        private final String productNameMid = "MID";
        private final String productNameNew = "NEW";

        // 판매 시작일
        private final LocalDate saleStartDateOld = LocalDate.of(2025, 1, 1);
        private final LocalDate saleStartDateMid = LocalDate.of(2025, 2, 1);
        private final LocalDate saleStartDateNew = LocalDate.of(2025, 3, 1);

        // 가격
        private final long priceHigh = 30000L;
        private final long priceLow = 10000L;
        private final long priceMid = 20000L;

        // 좋아요 수
        private final int likeCountHigh = 100;
        private final int likeCountMid = 10;
        private final int likeCountLow = 1;

        @BeforeEach
        void setUp() {
            brand = brandRepository.save(Brand.from("Nike", "Brand Desc", BrandStatus.ACTIVE));
            brandId = brand.getId();

            productRepository.save(createProduct(productNameOld, saleStartDateOld, priceHigh, likeCountMid));
            productRepository.save(createProduct(productNameMid, saleStartDateMid, priceLow, likeCountHigh));
            productRepository.save(createProduct(productNameNew, saleStartDateNew, priceMid, likeCountLow));
        }

        private Product createProduct(String name, LocalDate saleDate, long price, int likes) {
            return Product.from(name, price, ProductStatus.AVAILABLE, likes, Quantity.of(10), saleDate, brandId);
        }

        @Test
        @DisplayName("정렬/페이징이 없는 경우, 기본 정렬과 기본 페이지 크기로 해당 브랜드의 상품 목록을 반환한다.")
        void noSortAndPaging() {
            Optional<ProductSortType> emptySortType = Optional.empty();
            Optional<PagingCondition> emptyPagingCondition = Optional.empty();

            var actual = sut.findList(Optional.of(brandId), emptySortType, emptyPagingCondition); //todo: 쿼리에 limit 가 없다. count 조회 쿼리도 없고
            /**
             * select b1_0.id,b1_0.created_at,b1_0.deleted_at,b1_0.description,b1_0.name,b1_0.status,b1_0.updated_at from brand b1_0 where b1_0.id=?
             * Hibernate: select b1_0.id,b1_0.created_at,b1_0.deleted_at,b1_0.description,b1_0.name,b1_0.status,b1_0.updated_at from brand b1_0 where b1_0.id=?
             */
            assertAll(
                    () -> assertThat(actual.products())
                            .extracting(ListViewItemResult::brandId)
                            .allMatch(it -> it.equals(brandId)),
                    () -> assertThat(actual.pagination().totalCount()).isEqualTo(3), // 검색 조건에 맞는 전체 상품 수
                    () -> assertThat(actual.pagination().page()).isEqualTo(0), // 기본 페이지 번호 적용 확인
                    () -> assertThat(actual.products()).hasSize(3), // 현재 페이지에 포함된 상품 수
                    () -> assertThat(actual.products().size()).isLessThanOrEqualTo(PagingPolicy.PRODUCT.getDefaultPageSize()), // 기본 페이지 사이즈 초과여부 확인
                    () -> assertThat(actual.products()).extracting(ListViewItemResult::productName)
                            .containsExactly(productNameNew, productNameMid, productNameOld) // 최신순 정렬이 올바르게 적용되었는지 상품명 순서로 확인
            );
        }

        @Test
        @DisplayName("좋아요 순의 정렬 조건만 존재할 때, 지정한 정렬 기준과 기본 페이지 크기를 사용해 해당 브랜드의 상품 목록을 반환한다.")
        void sortOnly() {
            Optional<ProductSortType> productSortType = Optional.of(ProductSortType.LIKES_DESC);
            Optional<PagingCondition> emptyPagingCondition = Optional.empty();

            var result = sut.findList(Optional.of(brandId), productSortType, emptyPagingCondition);

            assertAll(
                    () -> assertThat(result.products())
                            .extracting(ListViewItemResult::brandId)
                            .allMatch(it -> it.equals(brandId)),
                    () -> assertThat(result.pagination().totalCount()).isEqualTo(3), // 검색 조건에 맞는 전체 상품 수
                    () -> assertThat(result.pagination().page()).isEqualTo(0), // 기본 페이지 번호가 0인지 확인
                    () -> assertThat(result.products().size()).isLessThanOrEqualTo(PagingPolicy.PRODUCT.getDefaultPageSize()), // 기본 페이지 사이즈 초과여부 확인
                    () -> assertThat(result.products())
                            .extracting(p -> p.likeCount())
                            .containsExactly(100, 10, 1) // 좋아요 수 기준 내림차순 정렬 확인
            );
        }

        @Test
        @DisplayName("페이징 조건만 존재할 때, 기본 정렬 기준으로 주어진 조건으로 페이징된 해당 브랜드의 상품 목록을 반환한다.")
        void pagingOnly() {
            Optional<ProductSortType> emptySortType = Optional.empty();
            Optional<PagingCondition> pagingCondition = Optional.of(new PagingCondition(0, 2));

            var result = sut.findList(Optional.of(brandId), emptySortType, pagingCondition);

            assertAll(
                    () -> assertThat(result.pagination().totalCount()).isEqualTo(3), // 검색 조건에 맞는 전체 상품 수
                    () -> assertThat(result.pagination().page()).isEqualTo(0), // 요청한 페이지 번호 확인
                    () -> assertThat(result.products()).hasSize(2), // 현재 페이지에 포함된 상품 수
                    () -> assertThat(result.products())
                            .extracting(ListViewItemResult::productName)
                            .containsExactly(productNameNew, productNameMid) // 최신순 정렬 기준에 따른 상품 순서 확인
            );
        }

        @Test
        @DisplayName("정렬/페이징 모두 존재할 때, 조건에 맞는 해당 브랜드의 상품 목록을 반환한다.")
        void  sortAndPaging() {
            Optional<ProductSortType> sortType = Optional.of(ProductSortType.LIKES_DESC);
            Optional<PagingCondition> pagingCondition = Optional.of(new PagingCondition(0, 2));

            var result = sut.findList(Optional.of(brandId), sortType, pagingCondition); //todo : 잘됨

            assertAll(
                    () -> assertThat(result.pagination().totalCount()).isEqualTo(3), // 검색 조건에 맞는 전체 상품 수
                    () -> assertThat(result.pagination().page()).isEqualTo(0), // 요청한 페이지 번호 확인
                    () -> assertThat(result.products()).hasSize(2), // 현재 페이지에 포함된 상품 수
                    () -> assertThat(result.products())
                            .extracting(p -> p.likeCount())
                            .containsExactly(100, 10) // 좋아요순 정렬 기준에 따른 상품 순서 확인
            );
        }

        @DisplayName("존재하지 않는 brandId를 전달하면, 빈 목록과 0개의 total count를 반환한다.")
        void invalidBrandId() {
            // arrange
            Long nonexistentBrandId = 9999L;
            boolean existsByBrandId = brandRepository.existsById(nonexistentBrandId);
            assertThat(existsByBrandId).isFalse();

            Optional<ProductSortType> emptySortType = Optional.empty();
            Optional<PagingCondition> emptyPagingCondition = Optional.empty();

            // act
            var actual = sut.findList(Optional.of(brandId), emptySortType, emptyPagingCondition);

            // assert
            assertAll(
                    () -> assertThat(actual.products()).isEmpty(), // 상품 목록이 비어 있음
                    () -> assertThat(actual.pagination().totalCount()).isZero(), // 전체 개수가 0
                    () -> assertThat(actual.pagination().page()).isEqualTo(0), // 기본 페이지 (0)
                    () -> assertThat(actual.pagination().size()).isEqualTo(PagingPolicy.PRODUCT.getDefaultPageSize()) // 기본 사이즈 사용
            );
        }
    }

    @DisplayName("brandId가 존재하는 경우,")
    @Nested
    class WhenBrandIdNotExists {
        private Brand brand;
        private Long brandId;

        @BeforeEach
        void setUp() {
            brand = brandRepository.save(Brand.from("Nike", "Brand Desc", BrandStatus.ACTIVE));
            brandId = brand.getId();
        }

        @Test
        @DisplayName("해당 브랜드에 상품이 하나도 없는 경우, 빈 목록과 0개의 total count를 반환한다.")
        void brandHasNoProducts() {
            // arrange
            productRepository.findAll().forEach(product -> //todo 제거예정
                    System.out.printf(
                            "[ID: %d, Name: %s, Status: %s, BrandId: %d]%n",
                            product.getId(),
                            product.getName(),
                            product.getStatus(),
                            product.getBrandId()
                    )
            );

            boolean existListViewableByBrandId = productRepository.existsListViewableByBrandId(brandId);
            assertThat(existListViewableByBrandId).isFalse();

            Optional<ProductSortType> emptySortType = Optional.empty();
            Optional<PagingCondition> emptyPagingCondition = Optional.empty();

            // act
            var actual = sut.findList(Optional.of(brandId), emptySortType, emptyPagingCondition);

            // assert
            assertAll(
                    () -> assertThat(actual.products()).isEmpty(), // 상품 목록이 비어 있음
                    () -> assertThat(actual.pagination().totalCount()).isZero(), // 전체 개수가 0
                    () -> assertThat(actual.pagination().page()).isEqualTo(0), // 기본 페이지 (0)
                    () -> assertThat(actual.pagination().size()).isEqualTo(PagingPolicy.PRODUCT.getDefaultPageSize()) // 기본 사이즈 사용
            );
        }
    }
}
