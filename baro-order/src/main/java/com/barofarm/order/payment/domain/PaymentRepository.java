package com.barofarm.order.payment.domain;

import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByPaymentKey(String paymentKey);
}
