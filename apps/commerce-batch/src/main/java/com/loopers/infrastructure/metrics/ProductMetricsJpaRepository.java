package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;

/*
JpaRepository 자체가 이미 PagingAndSortingRepository를 상속하므로, 사실 뒤에 PagingAndSortingRepository를 붙일 필요는 없습니다.
다만 이렇게 하면 IDE가 더 명확히 인식합니다.
 */
public interface ProductMetricsJpaRepository extends JpaRepository<ProductMetrics, Long>, PagingAndSortingRepository<ProductMetrics, Long> {

    Page<ProductMetrics> findAllByMetricsDateBetween(LocalDate from, LocalDate to, Pageable pageable);
}
