package com.barofarm.order.payment.infrastructure;

import com.barofarm.order.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<Payment, UUID> {
}
