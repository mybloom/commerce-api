package com.loopers.domain.payment;


import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long paymentId);

    Optional<Payment> findByOrderId(Long orderId);
}
