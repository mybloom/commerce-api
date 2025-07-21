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
        UserFacadeDto.SignUpResult signUpResult = userFacade.signUp(signUpRequest.toCommand());

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

    @GetMapping("/me")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> retrieveMyInfo(@RequestHeader(name = "X-USER-ID", required = true) Long id) {
        UserFacadeDto.MyInfoCriteria myInfoCriteria = userFacade.retrieveMyInfo(id);

        return ApiResponse.success(
                new UserV1Dto.UserResponse(
                        myInfoCriteria.id(),
                        myInfoCriteria.memberId(),
                        myInfoCriteria.email(),
                        myInfoCriteria.birthDate(),
                        UserV1Dto.Gender.valueOf(myInfoCriteria.gender().name())
                )
        );
    }
}
