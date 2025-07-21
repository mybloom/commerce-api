package com.loopers.domain.user;


import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceIntegrationTest {
    private static final String memberId = "testId";
    private static final String email = "test@test.com";
    private static final String birthDate = "2000-01-01";
    private static final Gender gender = Gender.MALE;

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
            boolean beforeSignUpExists = userJpaRepository.existsByMemberId(memberId);
            assertThat(beforeSignUpExists).isFalse();

            //act
            User savedUser = userService.save(memberId, email, birthDate, gender);

            //assert
            boolean afterSignUpExists = userJpaRepository.existsByMemberId(memberId);
            assertThat(afterSignUpExists).isTrue();

            // ArgumentCaptor 사용해 실제로 전달된 User 검증
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userJpaRepository, times(1)).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getMemberId()).isEqualTo(memberId);
        }

        @Test
        @DisplayName("이미 가입된 MemberID로 회원가입 시도 시, 예외가 발생한다.")
        void throwsException_whenMemberIdAlreadyExists() {
            //arrange: 이미 존재하는 사용자 저장
            String duplicatedMemberId = "dupUser";
            userJpaRepository.save(new User(duplicatedMemberId, email, birthDate, gender));

            boolean beforeSignUpExists = userJpaRepository.existsByMemberId(duplicatedMemberId);
            assertThat(beforeSignUpExists).isTrue();

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

        @DisplayName("해당 ID의 회원이 존재할 경우, 회원 정보가 반환된다.")
        @Test
        void returnUserInfo_whenUserExists() {
            //arrange
            User expectedUser = userJpaRepository.save(new User(memberId, email, birthDate, gender));

            //act
            User actualUser = userService.retrieveById(expectedUser.getId()).orElse(null);

            //assert
            assertThat(actualUser.getId()).isEqualTo(expectedUser.getId());
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, 비어있는 Optional이 반환된다.")
        @Test
        void returnNull_whenUserDoesNotExist() {
            //arrange
            Long nonexistentId = 1L;
            boolean existsBeforeRetrieve = userJpaRepository.existsById(nonexistentId);
            assertThat(existsBeforeRetrieve).isFalse();

            //act
            Optional<User> retrievedUser = userService.retrieveById(nonexistentId);

            //assert
            assertThat(retrievedUser).isEmpty();
        }
    }

}
