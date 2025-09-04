package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeResult;
import com.loopers.application.like.LikeUseCase;
import com.loopers.domain.like.LikeQuery;
import com.loopers.domain.like.LikeService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/like")
public class LikeV1ApiController {

    private final LikeUseCase likeUseCase;

    @PostMapping("/products/{productId}")
    public ApiResponse<LikeV1Dto.RegisterResponse> register(
            @RequestHeader(name = "X-USER-ID", required = true) final Long userId,
            @PathVariable final Long productId
    ) {
        LikeResult.LikeRegisterResult result = likeUseCase.register(userId, productId);
        return ApiResponse.success(LikeV1Dto.RegisterResponse.from(result));
    }

    @DeleteMapping("/products/{productId}")
    public ApiResponse<LikeV1Dto.RemoveResponse> remove(
            @RequestHeader(name = "X-USER-ID", required = true) final Long userId,
            @PathVariable final Long productId
    ) {
        LikeResult.LikeRemoveResult result = likeUseCase.remove(userId, productId);
        return ApiResponse.success(LikeV1Dto.RemoveResponse.from(result));
    }
}
