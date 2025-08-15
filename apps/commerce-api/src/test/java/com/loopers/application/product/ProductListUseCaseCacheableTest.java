package com.loopers.application.product;


import com.loopers.application.common.PagingCondition;
import com.loopers.domain.commonvo.LikeCount;
import com.loopers.domain.commonvo.Money;
import com.loopers.domain.product.ProductListProjection;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductSortType;
import com.loopers.domain.product.ProductStatus;
import com.loopers.support.cache.CacheTemplate;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProductListUseCaseCacheableTest {
    @Autowired
    private ProductListUseCase sut;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CacheTemplate cache;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private ProductListCachePolicy productListCachePolicy;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("비캐시 페이지(2페이지 이상)는 DB만 조회하고 캐시는 호출하지 않는다")
    void retrieveList_nonCachePage_callsDbOnly() {
        int pageNumber = 2;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, ProductSortType.LATEST.toSort()); // 2페이지 → 비캐시
        // DB가 빈 페이지를 반환한다고 가정
        when(productService.retrieveList(pageable)).thenReturn(Page.empty(pageable));

        var result = sut.findList(
                Optional.empty(), Optional.ofNullable(ProductSortType.LATEST), Optional.ofNullable(PagingCondition.create(pageNumber, pageSize))
        );

        assertNotNull(result);
        // 캐시는 호출되지 않음
        verify(cache, never()).getOrLoad(anyString(), anyString(), any(), any(), any(), any());
        // DB는 1회 호출
        verify(productService, times(1)).retrieveList(pageable);
    }

    @Test
    @DisplayName("캐시 대상(0페이지): 캐시 미스 시 loader(DB) 실행하여 결과 반환한다")
    void findList_cachePage0_miss_thenLoadFromDb() {
        // given
        int pageNumber = 0;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, ProductSortType.LATEST.toSort());

        // ProductListProjection 실제 객체 생성
        ProductListProjection p1 = new ProductListProjection(
                1L,
                "샘플 상품",
                Money.of(1000L),
                LikeCount.from(10),
                ProductStatus.AVAILABLE,
                LocalDate.now(),
                ZonedDateTime.now(),
                101L,
                "브랜드명"
        );

        Page<ProductListProjection> dbPage = new PageImpl<>(List.of(p1), pageable, 1);

        // DB가 페이지 반환
        when(productService.retrieveList(pageable)).thenReturn(dbPage);

        // 캐시 미스 상황 → loader 실행 후 반환
        when(cache.getOrLoad(anyString(), anyString(), any(), any(), any(), any()))
                .thenAnswer(inv -> {
                    @SuppressWarnings("unchecked")
                    Supplier<Object> loader = (Supplier<Object>) inv.getArgument(3); // loader
                    return loader.get(); // PagePayload.from(DB page) 실행
                });

        // when
        var result = sut.findList(
                Optional.empty(),
                Optional.of(ProductSortType.LATEST),
                Optional.of(PagingCondition.create(pageNumber, pageSize))
        );

        // then
        assertNotNull(result);
        assertThat(result.products().size()).isEqualTo(1);

        verify(cache, times(1)).getOrLoad(anyString(), anyString(), any(), any(), any(), any());
        verify(productService, times(1)).retrieveList(pageable);  // DB 호출됨
    }

    @Test
    @DisplayName("캐시 대상(1페이지): 캐시 히트 시 DB를 호출하지 않는다")
    void findList_cachePage1_hit_noDbCall() {
        // given
        int pageNumber = 1;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, ProductSortType.LATEST.toSort());

        // ProductListProjection 실제 객체 생성
        ProductListProjection p1 = new ProductListProjection(
                1L,
                "샘플 상품",
                Money.of(1000L),
                LikeCount.from(10),
                ProductStatus.AVAILABLE,
                LocalDate.now(),
                ZonedDateTime.now(),
                101L,
                "브랜드명"
        );

        List<ProductListProjection> items = List.of(p1);
        long total = 1L;

        // 캐시에서 바로 PagePayload 반환하도록 설정
        when(cache.getOrLoad(anyString(), anyString(), any(), any(), any(), any()))
                .thenAnswer(inv -> {
                    Class<?> payloadType = inv.getArgument(2); // PagePayload.class
                    return newPayload(payloadType, items, total);
                });

        // when
        var actual = sut.findList(
                Optional.empty(),
                Optional.of(ProductSortType.LATEST),
                Optional.of(PagingCondition.create(pageNumber, pageSize))
        );

        // then
        assertNotNull(actual);
        assertThat(actual.products().size()).isEqualTo(1);

        verify(cache, times(1)).getOrLoad(anyString(), anyString(), any(), any(), any(), any());
        verify(productService, never()).retrieveList(pageable);    // DB 미호출
    }

    // --- helper: 내부 private PagePayload(List<ProductListProjection>, long) 생성 ---
    private Object newPayload(Class<?> payloadType,
                              List<ProductListProjection> items,
                              long total) {
        try {
            var ctor = payloadType.getDeclaredConstructor(List.class, long.class);
            ctor.setAccessible(true);
            return ctor.newInstance(items, total);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PagePayload via reflection", e);
        }
    }
}
