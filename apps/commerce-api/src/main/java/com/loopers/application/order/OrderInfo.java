package com.loopers.application.order;

import java.util.List;

public class OrderInfo {
    public record ItemInfo(
        Long productId,
        String productName,
        int quantity,
        Long price,
        Long subTotal
    ) {
    }

}
