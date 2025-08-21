package com.loopers.domain.product;

import com.loopers.domain.commonvo.Quantity;

public class ProductCommandOld {

    public record CheckStock(
            Long productId,
            Quantity quantity
    ) {
        public static CheckStock of(Long productId, Quantity quantity) {
            return new CheckStock(productId, quantity);
        }
    }

    public record DeductStock(
            Product product,
            Quantity quantity
    ) {
        public static DeductStock of(Product product, Quantity quantity) {
            return new DeductStock(product, quantity);
        }
    }
}
