package com.loopers.domain.like;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.application.like.LikeResult.LikeDetailResult;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandStatus;
import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LikeProductService 단위 테스트")
class LikeProductServiceTest {

    private LikeProductService sut;

    @BeforeEach
    void setUp() {
        sut = new LikeProductService();
    }

    @Test
    @DisplayName("LikeHistory, Product, Brand 정보를 조합해 LikeDetailResult 목록을 반환한다")
    void assembleLikeDetails_successfully() {
        // Arrange
        Long userId = 1L;
        Long productId = 0L;
        Long brandId = 0L;

        LikeHistory likeHistory = LikeHistory.from(userId, productId);
        Product product = Product.testInstance(
            "Test Product",
            1000L,
            ProductStatus.AVAILABLE,
            0,
                Quantity.of(10),
            LocalDate.now().plusDays(10),
            brandId
        );

        Brand brand = Brand.testInstance("Nike", "Just Do It", BrandStatus.ACTIVE);

        List<LikeHistory> histories = List.of(likeHistory);
        List<Product> products = List.of(product);
        List<Brand> brands = List.of(brand);

        // Act
        List<LikeDetailResult> results = sut.assembleLikeProductInfo(histories, products, brands);

        // Assert
        assertAll(
            () -> assertThat(results).hasSize(1),
            () -> assertThat(results.get(0).productId()).isEqualTo(productId),
            () -> assertThat(results.get(0).productName()).isEqualTo("Test Product"),
            () -> assertThat(results.get(0).brandName()).isEqualTo("Nike")
        );
    }

    @Test
    @DisplayName("상품이나 브랜드 정보가 없으면, NPE가 발생할 수 있다")
    void assembleLikeDetails_missingData() {
        // Arrange
        Long userId = 1L;
        Long productId = 100L;
        Long brandId = 10L;

        LikeHistory likeHistory = LikeHistory.from(userId, productId);

        // 상품 정보는 없음
        List<LikeHistory> histories = List.of(likeHistory);
        List<Product> products = List.of(); // 비어 있음
        List<Brand> brands = List.of(); // 비어 있음

        // Act & Assert
        assertThatThrownBy(() -> sut.assembleLikeProductInfo(histories, products, brands))
            .isInstanceOf(NullPointerException.class);
    }
}
