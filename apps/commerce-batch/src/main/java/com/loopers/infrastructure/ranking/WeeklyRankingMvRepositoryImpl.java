package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.WeeklyRankingMvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@RequiredArgsConstructor
@Repository
public class WeeklyRankingMvRepositoryImpl implements WeeklyRankingMvRepository {

    private final WeeklyRankingMvJpaRepository weeklyRankingMvJpaRepository;

    @Override
    public int upsert(LocalDate aggDate, Long productId, Double score) {
        return weeklyRankingMvJpaRepository.upsert(aggDate, productId, score);
    }
}
