package com.loopers.domain.commonvo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Objects;
import lombok.Getter;

@Getter
public class LikeCount implements Comparable<LikeCount> {
    private final int value;

    private LikeCount(int value) {
        if (value < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "좋아요 수는 음수일 수 없습니다.");
        }
        this.value = value;
    }

    public static LikeCount from(int value) {
        return new LikeCount(value);
    }

    public static LikeCount zero() {
        return new LikeCount(0);
    }

    public LikeCount increment() {
        return new LikeCount(this.value + 1);
    }

    public LikeCount decrement() {
        if (this.value == 0) {
            return this;
        }
        return new LikeCount(this.value - 1);
    }

    @Override
    public int compareTo(LikeCount other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LikeCount likeCount = (LikeCount) o;
        return value == likeCount.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
