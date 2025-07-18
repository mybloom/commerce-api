package com.loopers.domain.point;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.point.PointJpaRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PointServiceIntegrationTest {


    @Autowired
    private PointService pointService;

    @MockitoBean
    private PointJpaRepository pointJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("포인트 충전 시,")
    @Nested
    class charge {

        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 사용자 Not Found 예외가 발생한다.")
        @Test
        void throwsException_whenUserDoesNotExist() {
            // arrange
            Long notExistUserId = 9999L;
            Long chargeAmount = 1000L;

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                pointService.charge(notExistUserId, chargeAmount);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            verify(pointJpaRepository, never()).save(org.mockito.Mockito.any());
        }
    }

    @DisplayName("포인트 조회 시,")
    @Nested
    class retrieve {
        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void returnsPointBalance_whenUserExists() {
            // arrange
            Long userId = 1L;
            Long expectedBalance = 5000L;

            Point mockPoint = mock(Point.class);
            when(mockPoint.getAmount()).thenReturn(expectedBalance);
            when(pointJpaRepository.findByUserId(userId)).thenReturn(Optional.of(mockPoint));

            // act
            Point actualPoint = pointService.retrieve(userId).orElse(null);

            // assert
            assertThat(actualPoint.getAmount()).isEqualTo(expectedBalance);
            verify(pointJpaRepository, times(1)).findByUserId(userId);
        }

        @Test
        @DisplayName("포인트 조회시 해당 ID 의 회원이 존재하지 않을 경우, 비어있는 Optional이 반환된다.")
        void returnNull_whenUserDoesNotExist() {
            // arrange
            Long notExistUserId = 999L;

            // act
            Optional<Point> actualPoint = pointService.retrieve(notExistUserId);

            // assert
            assertThat(actualPoint).isEmpty();
        }
    }
}
