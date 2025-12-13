package com.barofarm.order.deposit.infrastructure;

import com.barofarm.order.deposit.domain.Deposit;
import com.barofarm.order.deposit.domain.DepositRepository;

import java.util.Optional;
import java.util.UUID;

public class DepositChargeRepositoryAdapter implements DepositRepository {
    @Override
    public Optional<Deposit> findById(UUID depositId) {
        return Optional.empty();
    }

    @Override
    public Optional<Deposit> findByUserId(UUID userId) {
        return Optional.empty();
    }

    @Override
    public Deposit save(Deposit deposit) {
        return null;
    }
}
