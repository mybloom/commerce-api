package com.loopers.application.order;

import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.order.OrderLineCommand;

import java.util.List;

public class OrderInfo {
    public record ItemInfo(
        Long productId,
        int quantity
    ) {
    }

    public static List<OrderLineCommand> toCommands(List<ItemInfo> items) {
        return items.stream()
                .map(item -> new OrderLineCommand(
                        item.productId(),
                        Quantity.of(item.quantity())
                ))
                .toList();
    }
}
