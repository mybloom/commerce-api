package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "User V1 API", description = "User management operations")
public interface UserV1ApiSpec {

    // 이 클래스는 UserV1 API의 스펙을 정의합니다.
    // Swagger/OpenAPI 문서화에 사용됩니다.
    //친절하게 작성하려면 스웨거 이그잼플 애너테이션을 많이 달아야 되여. 그럼 그게 비즈니스 코드인지 아닌지 헷갈린다.
    // 그러다보니 따로 선언한다.

    @Operation(summary = "회원가입")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 가입 성공")
    ApiResponse<UserV1Dto.UserResponse> signUp(
            UserV1Dto.SignUpRequest signUpRequest
    );

}
