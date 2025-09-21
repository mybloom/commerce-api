package com.loopers.domain.weeklymetrics;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.infrastructure.weeklymetrics.ProductMetricsWeeklyJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class WeeklyMetricsService {

    private final ProductMetricsWeeklyJpaRepository repository;

    public void aggregateAndSave(List<ProductMetrics> metricsList, LocalDate from, LocalDate to) {
        // productId별 그룹핑
        Map<Long, List<ProductMetrics>> grouped =
                metricsList.stream().collect(Collectors.groupingBy(ProductMetrics::getProductId));

        grouped.forEach((productId, list) -> {
            long totalView = list.stream().mapToLong(ProductMetrics::getViewCount).sum();
            long totalPurchase = list.stream().mapToLong(ProductMetrics::getPurchaseCount).sum();
            long totalLike = list.stream().mapToLong(ProductMetrics::getLikeCount).sum();

            // ✅ ProductMetricsWeekly 엔티티 생성
            ProductMetricsWeekly weekly = ProductMetricsWeekly.from(
                    new ProductMetricsWeeklyCommand.Create(
                            productId, totalView, totalLike, totalPurchase, from, to
                    )
            );

            // ✅ 통째로 넘겨줌
            repository.upsert(weekly);
        });
    }
}
