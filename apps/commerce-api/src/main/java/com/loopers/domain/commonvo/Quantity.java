package com.loopers.domain.commonvo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quantity {

    private int amount;

    private Quantity(int amount) {
        this.amount = amount;
    }

    public static Quantity of(int amount) {
        if (amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 0보다 커야 합니다.");
        }

        return new Quantity(amount);
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.amount + other.amount);
    }

    public Quantity subtract(Quantity other) {
        int result = this.amount - other.amount;
        if (result <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 0보다 커야 합니다.");
        }
        return new Quantity(result);
    }

    public boolean isPositive() {
        return this.amount > 0;
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
