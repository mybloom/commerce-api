package com.loopers.domain.ranking;

public class RankingQuery {
    public record RankingItem(
          int rank,
          Long productId,
          double score
    ){}
}
