package com.barofarm.payment.payment.domain;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByPaymentKey(String paymentKey);

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findTopByUserIdAndPurposeOrderByCreatedAtDesc(UUID userId, Purpose purpose);
}
