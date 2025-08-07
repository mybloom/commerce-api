package com.loopers.domain.order;

import com.loopers.domain.product.Product;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrderLineService {

    public List<OrderLine> createOrderLines(final List<OrderLineCommand> lines, final List<Product> products) {
        final Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<OrderLine> orderLines = new ArrayList<>();
        for (OrderLineCommand line : lines) {
            final Product product = Optional.ofNullable(productMap.get(line.productId()))
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품 없음: " + line.productId()));
            orderLines.add(OrderLine.create(line.productId(), line.quantity(), product.getPrice()));
        }
        return orderLines;
    }
}
