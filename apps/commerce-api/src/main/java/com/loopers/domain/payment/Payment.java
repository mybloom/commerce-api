package com.loopers.domain.payment;

import com.loopers.domain.commonvo.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "payment")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    @Embedded
    private Money amount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "used_point"))
    private Money usedPoint;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime deletedAt;


    private Payment(Long orderId, Money amount, Money usedPoint) {
        this.orderId = orderId;
        this.amount = amount;
        this.usedPoint = usedPoint;
        this.createdAt = ZonedDateTime.now();
    }

    public Payment confirm(Long orderId, Money amount, Money usedPoint) {
        return new Payment(orderId, amount, usedPoint);
    }
}
