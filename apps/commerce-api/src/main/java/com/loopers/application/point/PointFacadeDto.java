package com.loopers.application.point;

import com.loopers.domain.point.Point;

public class PointFacadeDto {

    public record ChargeResult(
            Long amount
    ) {
        public static ChargeResult from(Point point) {
            return new ChargeResult(point.balance().getAmount());
        }
    }

    public record RetrieveResult(
            Long amount
    ) {
        public static RetrieveResult of(Long amount) {
            return new RetrieveResult(amount);
        }
    }
}
