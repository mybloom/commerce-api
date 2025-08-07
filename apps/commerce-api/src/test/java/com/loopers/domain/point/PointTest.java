package com.loopers.domain.point;

import com.loopers.domain.commonvo.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.constraints.Null;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PointTest {
    private static final Long validUserId = 1L;

    @DisplayName("포인트를 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("Point 객체 생성 시, 잔액은 0으로 초기화된다.")
        @Test
        void amountDefaultsToZero_whenCreated() {
            // act
            Point point = Point.createInitial(validUserId);

            // assert
            assertThat(point.balance().getAmount()).isEqualTo(0L);
        }
    }

    @DisplayName("포인트를 충전할 때, ")
    @Nested
    class Charge {

        @DisplayName("0 이하의 정수로 포인트를 충전 시, Bad Request 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(longs = {
                0L,
                -1L
        })
        void throwsBadRequestException_whenAmountIsZeroOrNegative(Long invalidAmount) {
            //arrange
            Point point = Point.createInitial(validUserId);

            //act
            CoreException exception = assertThrows(CoreException.class, () -> {
                point.charge(Money.of(invalidAmount));
            });

            //assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("1이상의 정수로 포인트 충전 시, 정상적으로 포인트가 충전된다.")
        @Test
        void chargePoint_whenAmountIsPositive() {
            // arrange
            Point point = Point.createInitial(validUserId);
            Long initialBalance = point.balance().getAmount();

            // act
            long chargeAmount = 1000L;
            point.charge(Money.of(chargeAmount));

            // assert
            assertThat(point.balance().getAmount()).isEqualTo(initialBalance + chargeAmount);
        }
    }

    @DisplayName("포인트를 사용할 때, ")
    @Nested
    class Use {

        @DisplayName("null 또는 0보다 작은 금액으로 사용 시 , Bad Request 예외가 발생한다.")
        @ParameterizedTest
        @NullSource
        @ValueSource(longs = {-100L, -1L})
        void throwsBadRequestException_whenAmountIsNullOrNegative(Long invalidAmount) {
            // arrange
            Point point = Point.createInitial(validUserId);
            point.charge(Money.of(1000L)); // 충분한 잔액 확보

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                point.use(Money.of(invalidAmount));
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("보유 포인트보다 많은 금액을 사용하려 할 경우, Conflict 예외가 발생한다.")
        @Test
        void throwsConflictException_whenAmountExceedsBalance() {
            // arrange
            Point point = Point.createInitial(validUserId);
            point.charge(Money.of(500L)); // 잔액 적게 설정

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                point.use(Money.of(1000L));
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        }

        @DisplayName("보유 포인트 이하의 금액을 사용할 경우, 잔액이 정상적으로 감소한다.")
        @ParameterizedTest
        @ValueSource(longs = {1000L, 900L, 0L})
        void deductBalance_whenAmountIsValid(long validAmount) {
            // arrange
            Point point = Point.createInitial(validUserId);
            Money initialAmount = Money.of(1000L);
            point.charge(initialAmount);
            Money useAmount = Money.of(validAmount);

            // act
            point.use(useAmount);

            // assert
            assertThat(point.balance()).isEqualTo(initialAmount.subtract(useAmount));
        }
    }
}
