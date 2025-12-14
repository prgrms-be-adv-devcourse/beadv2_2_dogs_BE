package com.barofarm.order.deposit.infrastructure;

import com.barofarm.order.deposit.domain.Deposit;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositJpaRepository extends JpaRepository<Deposit, UUID> {

    Optional<Deposit> findByUserId(UUID userId);
}
