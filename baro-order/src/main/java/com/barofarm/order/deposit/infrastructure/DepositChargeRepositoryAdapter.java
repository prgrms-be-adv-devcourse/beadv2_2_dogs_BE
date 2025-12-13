package com.barofarm.order.deposit.infrastructure;

import com.barofarm.order.deposit.domain.DepositCharge;
import com.barofarm.order.deposit.domain.DepositChargeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DepositChargeRepositoryAdapter implements DepositChargeRepository {

    private final DepositChargeJpaRepository depositChargeJpaRepository;

    @Override
    public DepositCharge save(DepositCharge deposit) {
        return depositChargeJpaRepository.save(deposit);
    }

    @Override
    public Optional<DepositCharge> findById(UUID depositChargeId) {
        return Optional.empty();
    }
}
