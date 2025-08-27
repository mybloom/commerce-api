package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.CardPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardPaymentJpaRepository extends JpaRepository<CardPayment, Long> {
    Optional<CardPayment> findByPaymentIdAndTransactionKey(Long paymentId, String transactionKey);
}
