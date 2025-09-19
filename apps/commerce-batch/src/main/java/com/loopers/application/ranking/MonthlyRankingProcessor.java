package com.loopers.application.ranking;

import com.loopers.domain.ranking.RankingResult;
import com.loopers.domain.weeklymetrics.ProductMetricsWeekly;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


@Component
public class MonthlyRankingProcessor implements ItemProcessor<ProductMetricsWeekly, RankingResult> {

    @Override
    public RankingResult process(ProductMetricsWeekly item) {
        // 1. raw score 계산
        double rawScore = (item.getLikeCount() * 1)
                + (item.getPurchaseCount() * 5)
                + (item.getViewCount() * 0.2);

        // 2. 날짜 가중치 (aggDate(=WindowEnd) 기준)
        long daysAgo = ChronoUnit.DAYS.between(item.getWindowEnd(), LocalDate.now());
        double weight = Math.max(0.4, 1.0 - (daysAgo * 0.1));

        // 3. 최종 점수 (가중치 반영)
        double weightedScore = rawScore * weight;

        return new RankingResult(
                item.getProductId(),
                item.getWindowEnd(),
                weightedScore
        );
    }
}
