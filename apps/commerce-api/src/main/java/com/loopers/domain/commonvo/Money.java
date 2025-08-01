package com.loopers.domain.commonvo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Objects;
import lombok.Getter;

@Getter
public class Money {
    public static final Money ZERO = new Money(0L);

    private final Long amount;

    private Money(Long amount) {
        this.amount = amount;
    }

    public static Money of(Long amount) {
        if (amount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "금액은 음수가 될 수 없습니다.");
        }
        return new Money(amount);
    }

    public Money add(Money other) {
        return new Money(this.amount + other.amount);
    }

    public Money subtract(Money other) {
        Long result = this.amount - other.amount;
        if (result < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "금액은 음수일 수 없습니다.");
        }
        return new Money(result);
    }

    public Money multiply(int multiplier) {
        return new Money(this.amount * multiplier);
    }

    public boolean isGreaterThan(Money other) {
        return this.amount > other.amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Money money = (Money) o;
        return Objects.equals(amount, money.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(amount);
    }
}
