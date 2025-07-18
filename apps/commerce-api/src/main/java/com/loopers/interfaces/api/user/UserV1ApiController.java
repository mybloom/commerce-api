package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserFacadeDto;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@RestController
public class UserV1ApiController implements UserV1ApiSpec {

    private final UserFacade userFacade;

    @PostMapping
    @Override
    public ApiResponse<UserV1Dto.UserResponse> signUp(@Valid @RequestBody UserV1Dto.SignUpRequest signUpRequest) {
        UserFacadeDto.SignUpResult signUpResult = userFacade.signUp(signUpRequest.toCriteria());

        return ApiResponse.success(
                new UserV1Dto.UserResponse(
                        signUpResult.id(),
                        signUpResult.memberId(),
                        signUpResult.email(),
                        signUpResult.birthDate(),
                        UserV1Dto.Gender.valueOf(signUpResult.gender().name())
                )
        );
    }

}
