package com.loopers.domain.ranking;

import com.loopers.infrastructure.ranking.MonthlyRankingMvJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class MonthlyRankingService {
    private final MonthlyRankingMvJpaRepository monthlyRankingMvRepository;

    @Transactional
    public void saveAll(List<RankingResult> items) {
        // productId 별 점수 합산
        Map<Long, Double> scoreByProduct = new HashMap<>();
        for (RankingResult r : items) {
            scoreByProduct.merge(r.getProductId(), r.getScore(), Double::sum);
        }

        // 집계 기준일 (오늘 날짜)
        LocalDate today = LocalDate.now();

        // upsert 실행
        scoreByProduct.forEach((productId, score) ->
                monthlyRankingMvRepository.upsert(today, productId, score));
    }
}
