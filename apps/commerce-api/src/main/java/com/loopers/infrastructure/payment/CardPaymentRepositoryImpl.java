package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.CardPayment;
import com.loopers.domain.payment.CardPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CardPaymentRepositoryImpl implements CardPaymentRepository {

    private final CardPaymentJpaRepository cardPaymentJpaRepository;

    @Override
    public CardPayment save(CardPayment cardPayment) {
        return cardPaymentJpaRepository.save(cardPayment);
    }

    @Override
    public Optional<CardPayment> findByPaymentIdAndTransactionKey(Long paymentId, String transactionKey) {
        return cardPaymentJpaRepository.findByPaymentIdAndTransactionKey(paymentId, transactionKey);
    }
}
