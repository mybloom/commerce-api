package com.loopers.domain.ranking;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RankingResult {
    private Long productId;
    private LocalDate aggDate;
    private double score;
    private int rankNo;

    public RankingResult(Long productId, LocalDate aggDate, double score) {
        this.productId = productId;
        this.aggDate = aggDate;
        this.score = score;
    }
}
