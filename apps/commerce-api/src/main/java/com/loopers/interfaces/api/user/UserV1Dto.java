package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacadeDto;
import jakarta.validation.constraints.NotNull;

public class UserV1Dto {
    public record SignUpRequest(
            @NotNull
            String memberId,
            @NotNull
            String email,
            @NotNull
            String birthDate,
            @NotNull
            Gender gender
    ) {
        public UserFacadeDto.SignUpCommand toCommand() {
            return new UserFacadeDto.SignUpCommand(
                    this.memberId,
                    this.email,
                    this.birthDate,
                    UserFacadeDto.Gender.valueOf(this.gender.name())
            );
        }
    }

    public record UserResponse(
            Long id,
            String memberId,
            String email,
            String birthDate,
            Gender gender
    ) {
    }

    public enum Gender {
        MALE, FEMALE
    }
}
