package com.loopers.domain.payment;

import java.util.Optional;

public interface CardPaymentRepository {
    CardPayment save(CardPayment cardPayment);

    Optional<CardPayment> findByPaymentIdAndTransactionKey(Long paymentId, String transactionKey);
}
