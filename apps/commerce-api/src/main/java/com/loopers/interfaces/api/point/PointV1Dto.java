package com.loopers.interfaces.api.point;

import jakarta.validation.constraints.NotNull;

public class PointV1Dto {

    public record ChargeRequest(
            @NotNull
            Long amount
    ) {
    }

    public record PointResponse(
            Long amount
    ) {
    }
}
