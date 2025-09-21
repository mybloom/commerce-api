package com.loopers.infrastructure.weeklymetrics;

import com.loopers.domain.weeklymetrics.ProductMetricsWeekly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProductMetricsWeeklyJpaRepository extends JpaRepository<ProductMetricsWeekly, Long> {

    @Modifying
    @Query(value = """
    INSERT INTO product_metrics_weekly 
        (product_id, window_start, window_end, like_count, purchase_count, view_count, created_at, updated_at)
    VALUES (:#{#w.productId}, :#{#w.windowStart}, :#{#w.windowEnd}, :#{#w.likeCount}, :#{#w.purchaseCount}, :#{#w.viewCount}, NOW(), NOW())
    ON DUPLICATE KEY UPDATE
        like_count      = product_metrics_weekly.like_count + VALUES(like_count),
        purchase_count  = product_metrics_weekly.purchase_count + VALUES(purchase_count),
        view_count      = product_metrics_weekly.view_count + VALUES(view_count),
        updated_at      = NOW()
    """, nativeQuery = true)
    void upsert(@Param("w") ProductMetricsWeekly w);

    Page<ProductMetricsWeekly> findAllByWindowEndIn(List<LocalDate> windowEndDates, Pageable pageable);
}
