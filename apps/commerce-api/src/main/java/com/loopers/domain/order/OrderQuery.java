package com.loopers.domain.order;

public class OrderQuery {
    public record CreatedOrder(
            Order order,
            boolean isNewlyCreated
    ) {
        public static CreatedOrder existing(Order order) {
            return new CreatedOrder(order, false);
        }

        public static CreatedOrder created(Order order) {
            return new CreatedOrder(order, true);
        }
    }

    public record RetrieveOrder(

    ){

    }
}
