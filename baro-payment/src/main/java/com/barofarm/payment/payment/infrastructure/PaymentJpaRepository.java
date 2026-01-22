package com.barofarm.payment.payment.infrastructure;

import com.barofarm.payment.payment.domain.Payment;
import com.barofarm.payment.payment.domain.Purpose;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentKey(String paymentKey);

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findTopByUserIdAndPurposeOrderByCreatedAtDesc(UUID userId, Purpose purpose);
}
