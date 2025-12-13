package com.barofarm.order.deposit.domain;

import java.util.Optional;
import java.util.UUID;

public interface DepositChargeRepository {

    DepositCharge save(DepositCharge deposit);

    Optional<DepositCharge> findById(UUID depositChargeId);

}
