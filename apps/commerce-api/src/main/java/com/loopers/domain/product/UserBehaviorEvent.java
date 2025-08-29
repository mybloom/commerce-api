package com.loopers.domain.product;

import java.util.Optional;

public class UserBehaviorEvent {

    public record ProductView(Optional<Long> userId, Long productId) { }
}
