package com.barofarm.order.deposit.infrastructure;

import com.barofarm.order.deposit.domain.DepositCharge;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositChargeJpaRepository extends JpaRepository<DepositCharge, UUID> {
}
