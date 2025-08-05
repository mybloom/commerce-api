package com.loopers.domain.order;

import com.loopers.domain.commonvo.Quantity;

public record OrderLineCommand (
        Long productId,
        Quantity quantity
) {}
