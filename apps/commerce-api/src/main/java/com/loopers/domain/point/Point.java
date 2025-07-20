package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "point")
public class Point {
    public static final long INITIAL_POINT_AMOUNT = 0L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, unique = true)
    private Long userId; //User의 ID를 참조

    public Point(Long userId) {
        this.userId = userId;
        this.amount = INITIAL_POINT_AMOUNT;
    }

    public Point(Long userId, Long amount) {
        this.userId = userId;
        this.amount = amount;
    }

    //TODO: 반환타입 Long -> void 로 변경
    public Long charge(Long amount) {
        if (amount == null || amount < 1) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전 포인트는 1 이상이어야 합니다.");
        }

        return this.amount += amount;
    }

    public Long balance() {
        return amount;
    }
}
