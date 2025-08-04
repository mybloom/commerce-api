package com.loopers.domain.order;

import com.loopers.domain.commonvo.Money;
import com.loopers.domain.commonvo.Quantity;
import jakarta.persistence.*;

import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "order_line")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    @Embedded
    @Column(name = "quantity", insertable = false, updatable = false)
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "quantity_amount"))
    })
    private Quantity quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "discount_amount"))
    })
    private Money price;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime deletedAt;

    public OrderLine(Long productId, Quantity quantity, Money price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public Money getSubTotal() {
        return price.multiply(quantity.getAmount());
    }

}
