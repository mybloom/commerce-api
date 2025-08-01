package com.loopers.domain.like;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class LikeHistoryTest {

    @DisplayName("좋아요 히스토리를 생성할 때, ")
    @Nested
    class create {

        @DisplayName("사용자ID, 상품ID가 null이 아니고 유효하면, LikeHistory 객체를 반환한다.")
        @Test
        void returnLikeHistory_whenValidParameter() {
            Long validUserId = 1L;
            Long validProductId = 1L;

            // act
            LikeHistory actual = LikeHistory.from(validUserId, validProductId);

            assertAll(
                    () -> assertThat(actual).isNotNull(),
                    () -> assertThat(actual.getUserId()).isEqualTo(validUserId),
                    () -> assertThat(actual.getProductId()).isEqualTo(validProductId)
            );
        }
    }
}
