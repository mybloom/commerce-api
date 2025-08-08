package com.loopers.domain.commonvo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quantity {

    public static final Quantity ZERO = new Quantity(0);
    private int amount;

    public static Quantity of(int amount) {
        if (amount < 1) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 1 이상이어야 합니다.");
        }

        return new Quantity(amount);
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.amount + other.amount);
    }

    public Quantity subtract(Quantity other) {
        int result = this.amount - other.amount;
        if (isNegative(result)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 0 이상이어야 합니다.");
        }

        return new Quantity(result);
    }

    public boolean isNegative(int value) {
        return value < 0;
    }

    public boolean isGreaterThan(Quantity other) {
        return this.amount > other.amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Quantity quantity = (Quantity) o;
        return amount == quantity.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(amount);
    }
}
