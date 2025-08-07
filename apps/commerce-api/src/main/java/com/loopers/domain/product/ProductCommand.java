package com.loopers.domain.product;

import com.loopers.domain.commonvo.Quantity;

public class ProductCommand {

    public record CheckStock(
            Long productId,
            Quantity quantity
    ) {
        public static CheckStock of(Long productId, Quantity quantity) {
            return new CheckStock(productId, quantity);
        }
    }
}
