package com.loopers.job.weeklyranking;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.ranking.RankingResult;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class WeeklyRankingProcessor implements ItemProcessor<ProductMetrics, RankingResult> {

    @Override
    public RankingResult process(ProductMetrics item) {
        // 1. raw score 계산
        double rawScore = (item.getLikeCount() * 1)
                + (item.getPurchaseCount() * 5)
                + (item.getViewCount() * 0.2);

        // 2. 날짜 가중치 계산
        long daysAgo = ChronoUnit.DAYS.between(item.getMetricsDate(), LocalDate.now());
        double weight = Math.max(0.4, 1.0 - (daysAgo * 0.1)); // 선형 감소, 최소 0.4

        // 3. 최종 점수 반환
        return new RankingResult(
                item.getProductId(),
                item.getMetricsDate(),
                rawScore * weight
        );
    }
}
