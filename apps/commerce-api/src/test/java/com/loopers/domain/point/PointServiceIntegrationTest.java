package com.loopers.domain.point;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
public class PointServiceIntegrationTest {


    @Autowired
    private PointService sut;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @MockitoSpyBean
    private PointJpaRepository pointRepository;

    @MockitoSpyBean
    private UserJpaRepository userRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("포인트 충전 시,")
    @Nested
    class Charge {

        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 사용자 Not Found 예외가 발생한다.")
        @Test
        void throwsException_whenUserDoesNotExist() {
            // arrange
            Long nonexistentUserId = 9999L;
            boolean exists = userRepository.existsById(nonexistentUserId);
            assertThat(exists).isFalse();

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                sut.charge(nonexistentUserId, 1000L);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            verify(pointRepository, never()).save(any(Point.class));
        }

        @DisplayName("존재하는 유저 ID로 충전하는 경우, 증가한 잔액을 저장하고 잔액을 반환한다.")
        @Test
        void successCharge() {
            // arrange
            User user = userRepository.save(new User("testId", "test@test.com", "2000-01-01", Gender.MALE));
            Point savedPoint = pointRepository.save(Point.createInitial(user.getId()));
            reset(pointRepository);
            Long initialBalance = savedPoint.balance().getAmount();
            Long chargeAmount = 1000L;

            // act
            Point actualPoint = sut.charge(user.getId(), chargeAmount);

            // assert
            assertThat(actualPoint.balance().getAmount()).isEqualTo(initialBalance + chargeAmount);
            verify(pointRepository, times(1)).save(any(Point.class));
        }
    }

    @DisplayName("포인트 조회 시,")
    @Nested
    class Retrieve {

        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void returnsPointBalance_whenUserExists() {
            // arrange
            Long userId = 1L;
            Money amount = Money.of(1000L);
            Point point = Point.create(userId, amount);
            pointRepository.save(point);

            // act
            Optional<Point> actualPoint = sut.retrieve(userId);

            // assert
            assertThat(actualPoint.get().balance()).isEqualTo(amount);
            verify(pointRepository, times(1)).findByUserId(userId);
        }

        @Test
        @DisplayName("포인트 조회시 해당 ID 의 회원이 존재하지 않을 경우, 비어있는 Optional이 반환된다.")
        void returnNull_whenUserDoesNotExist() {
            // arrange
            Long notExistUserId = 999L;

            // act
            Optional<Point> actualPoint = sut.retrieve(notExistUserId);

            // assert
            assertThat(actualPoint).isEmpty();
        }
    }

    @DisplayName("포인트 사용 시,")
    @Nested
    @Transactional
    class Use {
        private final Long userId = 1L;
        private final Money initialAmount = Money.of(1000L);
        private Point point;

        @BeforeEach
        void setUp() {
            point = Point.create(userId, initialAmount);
            pointRepository.save(point);
        }

        @DisplayName("보유 포인트 이하의 금액을 사용할 경우, 포인트가 정상적으로 차감된다.")
        @Test
        void usePoint_success() {
            // Arrange
            Money useAmount = Money.of(300L);

            // Act
            sut.useOrThrow(point, useAmount);

            // Assert
            Point actual = pointRepository.findByUserId(userId).orElseThrow();
            assertThat(actual.balance()).isEqualTo(initialAmount.subtract(useAmount));
            verify(pointRepository, times(1)).save(actual);
        }

    }
}
