package com.loopers.domain.user;


import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @MockitoSpyBean
    private UserJpaRepository userJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("회원 가입 시,")
    @Nested
    class SignUp {

        @Test
        @DisplayName("회원 가입시 User 저장이 수행된다.")
        void savesUser_whenSignUpSucceeds() {
            //arrange
            String memberId = "testuser";
            String email = "test@test.com";
            String birthDate = "2000-01-01";
            Gender gender = Gender.MALE;

            //act
            User savedUser = userService.save(memberId, email, birthDate, gender);

            //assert
            assertAll(
                    () -> assertThat(userJpaRepository.existsByMemberId(memberId)).isTrue(),
                    () -> assertThat(savedUser.getMemberId()).isEqualTo(memberId),
                    () -> assertThat(savedUser.getEmail()).isEqualTo(email),
                    () -> assertThat(savedUser.getBirthDate()).isEqualTo(birthDate),
                    () -> assertThat(savedUser.getGender()).isEqualTo(gender)
            );
            verify(userJpaRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("이미 가입된 ID로 회원가입 시도 시, 예외가 발생한다.")
        void throwsException_whenMemberIdAlreadyExists() {
            //arrange: 이미 존재하는 사용자 저장
            String duplicatedMemberId = "dupUser";
            String email = "dupUser@example.com";
            String birthDate = "1990-01-01";
            Gender gender = Gender.FEMALE;

            userJpaRepository.save(new User(duplicatedMemberId, email, birthDate, gender));
            reset(userJpaRepository); // reset spy 기록

            //act: 같은 ID로 회원가입 시도하면 예외 발생
            CoreException exception = assertThrows(CoreException.class, () -> {
                userService.save(duplicatedMemberId, "another@example.com", "2000-01-01", Gender.MALE);
            });

            //assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.CONFLICT);
            verify(userJpaRepository, never()).save(any(User.class));
        }
    }

    @DisplayName("회원 정보 조회 시,")
    @Nested
    class Retrieve {

        @DisplayName("해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.")
        @Test
        void returnUserInfo_whenUserExists() {
            //arrange
            String memberId = "test";
            String email = "test@example.com";
            String birthDate = "2000-01-01";
            Gender gender = Gender.MALE;
            User expectedUser = userJpaRepository.save(
                    new User(memberId, email, birthDate, gender)
            );

            //act
            User actualUser = userService.retrieveById(expectedUser.getId()).orElse(null);

            //assert
            assertAll(
                    () -> assertThat(actualUser.getId()).isEqualTo(expectedUser.getId()),
                    () -> assertThat(actualUser.getMemberId()).isEqualTo(memberId),
                    () -> assertThat(actualUser.getEmail()).isEqualTo(email),
                    () -> assertThat(actualUser.getBirthDate()).isEqualTo(birthDate)
            );
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, 비어있는 Optional이 반환된다.")
        @Test
        void returnNull_whenUserDoesNotExist() {
            //act
            Optional<User> user = userService.retrieveById(1L);

            //assert
            assertThat(user).isEmpty();
        }
    }

}
