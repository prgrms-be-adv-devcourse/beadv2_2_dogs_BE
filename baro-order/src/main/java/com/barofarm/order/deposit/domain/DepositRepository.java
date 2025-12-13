package com.barofarm.order.deposit.domain;

import java.util.Optional;
import java.util.UUID;

public interface DepositRepository {

    Optional<Deposit> findById(UUID depositId);

    Optional<Deposit> findByUserId(UUID userId);

    Deposit save(Deposit deposit);

}
