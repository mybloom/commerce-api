package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.MonthlyRankingMv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface MonthlyRankingMvJpaRepository extends JpaRepository<MonthlyRankingMv, Long> {


    @Modifying
    @Query(value = """
        INSERT INTO monthly_ranking_mv (agg_date, product_id, score)
        VALUES (:aggDate, :productId, :score)
        ON DUPLICATE KEY UPDATE score = score + VALUES(score)
    """, nativeQuery = true)
    int upsert(@Param("aggDate") LocalDate aggDate,
               @Param("productId") Long productId,
               @Param("score") Double score);
}
