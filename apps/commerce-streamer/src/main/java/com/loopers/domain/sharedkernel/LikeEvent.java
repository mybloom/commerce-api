package com.loopers.domain.sharedkernel;

public class LikeEvent {
    public record LikeCountIncreased(Long productId) {}
    public record LikeCountDecreased(Long productId) {}
}
