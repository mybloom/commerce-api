package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PointTest {

    @DisplayName("포인트를 충전할 때, ")
    @Nested
    class Charge {

        @DisplayName("0 이하의 정수로 포인트를 충전 시, Bad Request 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(longs = {
                0L,
                -1L
        })
        void throwsBadRequestException_whenAmountIsZeroOrNegative(Long amount) {
            //arrange
            Long userId = 1L;
            Point point = new Point(userId);

            //act
            CoreException exception = assertThrows(CoreException.class, () -> {
                point.charge(amount);
            });

            //assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

}
