package com.loopers.domain.ranking;

import java.time.LocalDate;

public interface WeeklyRankingMvRepository {
    int upsert(LocalDate aggDate, Long productId, Double score);
}
