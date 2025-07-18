package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointFacade;
import com.loopers.application.point.PointFacadeDto;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
@RestController
public class PointV1ApiController implements PointV1ApiSpec {

    private final PointFacade pointFacade;


    @PostMapping
    @Override
    public ApiResponse<PointV1Dto.PointResponse> charge(
            @RequestHeader(name = "X-USER-ID", required = true) Long userId,
            @Valid @RequestBody PointV1Dto.ChargeRequest request
    ) {
        PointFacadeDto.ChargeResult chargeResult = pointFacade.charge(userId, request);
        return ApiResponse.success(
                new PointV1Dto.PointResponse(chargeResult.amount())
        );
    }
}
