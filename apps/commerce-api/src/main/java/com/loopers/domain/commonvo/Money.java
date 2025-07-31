package com.loopers.domain.commonvo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

//TODO: VO에 getter는 필수일까?
@Getter
public class Money implements Comparable<Money> {

    private final Long amount;

    public Money(Long amount) {
        if (amount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "금액은 음수가 될 수 없습니다.");
        }
        this.amount = amount;
    }

    public static Money from(Long amount) {
        return new Money(amount);
    }

    public Money add(Long amount) {
        return new Money(this.amount + amount);
    }

    @Override
    public int compareTo(Money other) {
        return Long.compare(this.amount, other.amount);
    }
}
