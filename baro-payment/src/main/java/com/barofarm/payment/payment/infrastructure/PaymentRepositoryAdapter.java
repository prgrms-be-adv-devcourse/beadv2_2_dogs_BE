package com.barofarm.payment.payment.infrastructure;

import com.barofarm.payment.payment.domain.Payment;
import com.barofarm.payment.payment.domain.PaymentRepository;
import com.barofarm.payment.payment.domain.Purpose;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findByPaymentKey(String paymentKey) {
        return paymentJpaRepository.findByPaymentKey(paymentKey);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<Payment> findTopByUserIdAndPurposeOrderByCreatedAtDesc(UUID userId, Purpose purpose) {
        return paymentJpaRepository.findTopByUserIdAndPurposeOrderByCreatedAtDesc(userId, purpose);
    }
}
