package com.loopers.domain.ranking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface RankingRepository {
    Page<RankingQuery.RankingItem> findAll(LocalDate date, Pageable pageable);

    Long findRankByDateAndProductId(LocalDate date, Long productId);
}
