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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PointServiceIntegrationTest {


    @Autowired
    private PointService pointService;

    @MockitoSpyBean
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
            Long nonexistentUserId = 9999L;
            boolean exists = userJpaRepository.existsById(nonexistentUserId);
            assertThat(exists).isFalse();

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                pointService.charge(nonexistentUserId, 1000L);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            verify(pointJpaRepository, never()).save(any(Point.class));
        }

        @DisplayName("존재하는 유저 ID로 충전하는 경우, 증가한 잔액을 저장하고 잔액을 반환한다.")
        @Test
        void successCharge() {
            // arrange
            User user = userJpaRepository.save(new User("testId", "test@test.com", "2000-01-01", Gender.MALE));
            Point savedPoint = pointJpaRepository.save(new Point(user.getId()));
            reset(pointJpaRepository);
            Long initialBalance = savedPoint.balance();
            Long chargeAmount = 1000L;

            // act
            Long actualAmount = pointService.charge(user.getId(), chargeAmount);

            // assert
            assertThat(actualAmount).isEqualTo(initialBalance + chargeAmount);
            verify(pointJpaRepository, times(1)).save(any(Point.class));
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
            Long amount = 1000L;
            Point point = new Point(userId, amount);
            pointJpaRepository.save(point);

            // act
            Optional<Point> actualPoint = pointService.retrieve(userId);

            // assert
            assertThat(actualPoint.get().balance()).isEqualTo(amount);
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
