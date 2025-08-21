package com.loopers.domain.order;

import com.loopers.domain.product.Product;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrderLineService {

    public List<OrderLine> createOrderLines(final List<OrderLineCommand> lines, final List<Product> products) {
        //수량 일치 검증
        if (products.size() != lines.size()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 수와 주문 라인이 일치하지 않습니다.");
        }

        //  productId → Product 매핑
        final Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 요청 productID 목록
        final Set<Long> requestedIds = lines.stream()
                .map(OrderLineCommand::productId)
                .collect(Collectors.toSet());

        // ID 누락 검증
        final Set<Long> actualIds = productMap.keySet();
        if (!actualIds.containsAll(requestedIds)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 수와 주문 라인이 일치하지 않습니다.");
        }

        // 매핑 및 생성
        List<OrderLine> orderLines = new ArrayList<>();
        for (OrderLineCommand line : lines) {
            Product product = productMap.get(line.productId()); // null 아님이 보장됨
            orderLines.add(OrderLine.create(line.productId(), line.quantity(), product.getPrice()));
        }

        return orderLines;
    }
}
