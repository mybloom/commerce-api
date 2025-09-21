package com.loopers.application.ranking;

import com.loopers.domain.ranking.RankingResult;
import com.loopers.domain.ranking.WeeklyRankingMvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class WeeklyRankingService {

    private final WeeklyRankingMvRepository weeklyRankingMvRepository;

    @Transactional
    public void saveAll(List<RankingResult> items) {
        Map<Long, Double> scoreByProduct = new HashMap<>();
        for (RankingResult r : items) {
            scoreByProduct.merge(r.getProductId(), r.getScore(), Double::sum);
        }

        LocalDate today = LocalDate.now();
        scoreByProduct.forEach((productId, score) ->
                weeklyRankingMvRepository.upsert(today, productId, score));
    }
}
