package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserTest {
    private static final String validMemberId = "testId";
    private static final String validEmail = "test@test.com";
    private static final String validBirthDate = "2000-01-01";
    private static final Gender validGender = Gender.MALE;

    @DisplayName("사용자 모델을 생성할 때, ")
    @Nested
    class Create {
        @DisplayName("ID 가 영문 및 숫자 10자 이내 형식에 맞지 않으면, User 객체 생성에 실패하고 Bad Request 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(strings = {
                "invalid1234", //11자
                "invalid!@#", //특수문자 포함
                "한글",
                "한글123"
        })
        void throwsBadRequestException_whenIdIsNotAlphanumericOrTooLong(String inValidMemberId) {
            //act
            CoreException exception = assertThrows(CoreException.class, () -> {
                new User(
                        inValidMemberId,
                        validEmail,
                        validBirthDate,
                        validGender
                );
            });

            //assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("이메일이 xx@yy.zz 형식에 맞지 않으면, User 객체 생성에 실패하고 Bad Request 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(strings = {
                "plainaddress",         // @ 없음
                "missingdomain@",       // 도메인 없음
                "@missingusername.com", // 사용자 이름 없음
                "user@.com",            // 도메인 이름 없음
                "user@com",             // .확장자 없음
                "user@domain..com"      // 연속된 점
        })
        void throwsBadRequestException_whenEmailIsInvalidFormat(String invalidEmail) {
            //act
            CoreException exception = assertThrows(CoreException.class, () -> {
                new User(
                        validMemberId,
                        invalidEmail,
                        validBirthDate,
                        validGender
                );
            });

            //assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("생년월일이 yyyy-MM-dd 형식에 맞지 않으면, User 객체 생성에 실패하고 Bad Request 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(strings = {
                "19900101", //형식이 잘못됨
                "90-01-01", //형식이 잘못됨
                "1990/01/01", //형식이 잘못됨
                "1990-13-01", //월이 잘못됨
                "abcd-ef-gh"  //형식이 잘못됨
        })
        void throwsBadRequestException_whenBirthdateIsInvalidFormat(String invalidBirthDate) {
            //act
            CoreException exception = assertThrows(CoreException.class, () -> {
                new User(
                        validMemberId,
                        validEmail,
                        invalidBirthDate,
                        validGender
                );
            });

            //assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
