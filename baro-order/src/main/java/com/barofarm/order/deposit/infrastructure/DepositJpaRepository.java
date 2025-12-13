package com.barofarm.order.deposit.infrastructure;

import com.barofarm.order.deposit.domain.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DepositJpaRepository extends JpaRepository<Deposit, UUID> {

    Optional<Deposit> findByUserId(UUID userId);
}
