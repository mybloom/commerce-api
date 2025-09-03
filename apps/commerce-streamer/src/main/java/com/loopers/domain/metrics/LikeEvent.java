package com.loopers.domain.metrics;

public class LikeEvent {
    public record LikeCountIncreased(
            Long productId
    ) {
    }

    public record LikeCountDecreased(
            Long productId
    ) {
    }
}
