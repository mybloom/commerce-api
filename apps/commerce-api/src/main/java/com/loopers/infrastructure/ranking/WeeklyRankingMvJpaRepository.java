package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.WeeklyRankingMv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface WeeklyRankingMvJpaRepository extends JpaRepository<WeeklyRankingMv, Long> {
    Page<WeeklyRankingMv> allByAggDateOrderByScoreDescProductIdAsc(LocalDate date, Pageable pageable);
}
