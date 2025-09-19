package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MonthlyRankingMv;
import com.loopers.domain.ranking.RankingQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface MonthlyRankingMvJpaRepository extends JpaRepository<MonthlyRankingMv, Long> {
    Page<MonthlyRankingMv> allByAggDateOrderByScoreDescProductIdAsc(LocalDate date, Pageable pageable);
}
