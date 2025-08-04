package com.loopers.support.paging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.loopers.application.common.PagingCondition;
import com.loopers.domain.product.ProductSortType;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DisplayName("PageableFactory 단위 테스트")
class PageableFactoryTest {

    @Test
    @DisplayName("정렬과 페이징이 모두 비어있을 때, 기본 정렬과 기본 페이지 크기를 사용한 Pageable을 반환한다.")
    void createPageable_withDefaults() {
        // arrange
        Optional<ProductSortType> emptyProductSortType = Optional.empty();
        Optional<PagingCondition> emptyPagingCondition = Optional.empty();

        // act
        Pageable pageable = PageableFactory.from(
            emptyProductSortType,
            emptyPagingCondition,
            ProductSortType.DEFAULT,
            PagingPolicy.PRODUCT.getDefaultPageSize()
        );

        // assert
        assertAll(
            () -> assertThat(pageable.getPageNumber()).isEqualTo(0),
            () -> assertThat(pageable.getPageSize()).isEqualTo(PagingPolicy.PRODUCT.getDefaultPageSize()),
            () -> assertThat(pageable.getSort()).isEqualTo(ProductSortType.DEFAULT.toSort())
        );
    }

    @Test
    @DisplayName("정렬 조건이 '좋아요순'이고 페이징이 비어있는 경우, 좋아요 많은 순으로 정렬된 기본 페이지 크기의 Pageable을 반환한다.")
    void createPageable_withCustomSort() {
        // arrange
        Optional<ProductSortType> productSortType = Optional.of(ProductSortType.LIKES_DESC);
        Optional<PagingCondition> pagingCondition = Optional.empty();

        // act
        Pageable pageable = PageableFactory.from(
            productSortType,
            pagingCondition,
            ProductSortType.DEFAULT,
            PagingPolicy.PRODUCT.getDefaultPageSize()
        );

        // assert
        assertAll(
            () -> assertThat(pageable.getPageNumber()).isEqualTo(0),
            () -> assertThat(pageable.getPageSize()).isEqualTo(PagingPolicy.PRODUCT.getDefaultPageSize()),
            () -> assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "likeCount"))
        );
    }

    @Test
    @DisplayName("정렬 조건이 비어있고 페이징 조건이 주어질 경우, 기본 정렬된 해당 페이징을 사용한 Pageable을 반환한다.")
    void createPageable_withCustomPaging() {
        // arrange
        Optional<ProductSortType> productSortType = Optional.empty();
        Optional<PagingCondition> pagingCondition = Optional.of(new PagingCondition(3, 15));

        // act
        Pageable pageable = PageableFactory.from(
            productSortType,
            pagingCondition,
            ProductSortType.DEFAULT,
            PagingPolicy.PRODUCT.getDefaultPageSize()
        );

        // assert
        assertAll(
            () -> assertThat(pageable.getPageNumber()).isEqualTo(3),
            () -> assertThat(pageable.getPageSize()).isEqualTo(15),
            () -> assertThat(pageable.getSort()).isEqualTo(ProductSortType.DEFAULT.toSort())
        );
    }
}
