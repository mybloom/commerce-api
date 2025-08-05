package com.loopers.domain.order;

public class OrderQuery {
    public record ResolvedOrderQuery(
            Order order,
            boolean isNewlyCreated
    ) {
        public static ResolvedOrderQuery existing(Order order) {
            return new ResolvedOrderQuery(order, false);
        }

        public static ResolvedOrderQuery created(Order order) {
            return new ResolvedOrderQuery(order, true);
        }
    }
}
