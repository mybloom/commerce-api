package com.loopers.application.user;

import com.loopers.domain.user.User;

public class UserFacadeDto {
    public record SignUpCriteria(
            String memberId,
            String email,
            String birthDate,
            Gender gender
    ) {
    }

    public record SignUpResult(
            Long id,
            String memberId,
            String email,
            String birthDate,
            Gender gender
    ) {
        public static SignUpResult from(User user) {
            return new SignUpResult(
                    user.getId(),
                    user.getMemberId(),
                    user.getEmail(),
                    user.getBirthDate(),
                    Gender.valueOf(user.getGender().name())
            );
        }
    }


    public enum Gender {
        MALE, FEMALE
    }
}
