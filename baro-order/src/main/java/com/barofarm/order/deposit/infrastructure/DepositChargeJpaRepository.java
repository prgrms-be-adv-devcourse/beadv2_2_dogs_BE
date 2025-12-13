package com.barofarm.order.deposit.infrastructure;

import com.barofarm.order.deposit.domain.DepositCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DepositChargeJpaRepository extends JpaRepository<DepositCharge, UUID> {
}
