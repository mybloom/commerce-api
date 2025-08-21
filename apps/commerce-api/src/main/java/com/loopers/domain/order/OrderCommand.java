package com.loopers.domain.order;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.commonvo.Quantity;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCommand;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderCommand {

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Complete {
        private final Long orderId;
        private final List<OrderLine> orderLines;
        private final Money orderAmount;
        private final Money discountAmount;

        public static Complete of(Long orderId,
                                  List<Product> products,
                                  ProductCommand.OrderProducts productCommand,
                                  Money orderAmount,
                                  Money discountAmount
        ) {
            // ProductCommand와 Product를 매핑하여 OrderLine 생성 정보 만들기
            Map<Long, Quantity> quantityMap = productCommand.getProducts().stream()
                    .collect(Collectors.toMap(
                            ProductCommand.OrderProducts.OrderProduct::getProductId,
                            ProductCommand.OrderProducts.OrderProduct::getQuantity
                    ));

            List<OrderLine> orderLines = products.stream()
                    .map(product ->
                            OrderLine.create(
                                    product.getId(),
                                    quantityMap.get(product.getId()),
                                    product.getPrice()
                            )
                    )
                    .toList();

            return Complete.builder()
                    .orderId(orderId)
                    .orderLines(orderLines)
                    .orderAmount(orderAmount)
                    .discountAmount(discountAmount)
                    .build();
        }
    }
}
