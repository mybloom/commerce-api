package com.loopers.domain.point;

import com.loopers.domain.commonvo.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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

}
