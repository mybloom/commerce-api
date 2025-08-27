package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "card_payment")
@Entity
public class CardPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * PG 거래 키
     */
    @Column(name = "transaction_key", nullable = false, unique = true)
    private String transactionKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = true)
    private CardPaymentStatus status;

    //비어있을 경우 성공, 값이 있을 경우 실패
    @Column(name = "reason", nullable = true)
    private String reason;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    public static CardPayment createInit(Long paymentId, String transactionKey) {
        return CardPayment.builder()
                .paymentId(paymentId)
                .transactionKey(transactionKey)
                .status(CardPaymentStatus.PENDING)
                .build();
    }

    public void success(String reason) {
        if(this.status != CardPaymentStatus.PENDING) {
            throw new CoreException(ErrorType.CONFLICT, "성공 처리를 할 수 없습니다. status=" + this.status);
        }
        this.status = CardPaymentStatus.SUCCESS;
        this.reason = reason;
    }

    public void fail(String reason) {
        if(this.status != CardPaymentStatus.PENDING) {
            throw new CoreException(ErrorType.CONFLICT, "실패 처리를 할 수 없습니다. status=" + this.status);
        }
        this.status = CardPaymentStatus.FAILED;
        this.reason = reason;
    }
}
