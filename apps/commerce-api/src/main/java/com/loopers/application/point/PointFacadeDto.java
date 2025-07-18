package com.loopers.application.point;

public class PointFacadeDto {

    public record ChargeResult(
            Long amount
    ) {
        public static ChargeResult of(Long amount) {
            return new ChargeResult(amount);
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
