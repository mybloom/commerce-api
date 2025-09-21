package com.loopers.domain.ranking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface RankingRepository {
    Page<RankingQuery.RankingItem> findDailyAll(LocalDate date, Pageable pageable);

    Long findDailyRankByDateAndProductId(LocalDate date, Long productId);

    Page<RankingQuery.RankingItem> findWeeklyAll(LocalDate date, Pageable pageable);

    Page<RankingQuery.RankingItem> findMonthlyAll(LocalDate date, Pageable pageable);
}
