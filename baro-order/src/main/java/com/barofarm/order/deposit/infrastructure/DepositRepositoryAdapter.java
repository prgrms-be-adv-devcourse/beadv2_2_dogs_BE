package com.barofarm.order.deposit.infrastructure;

import com.barofarm.order.deposit.domain.Deposit;
import com.barofarm.order.deposit.domain.DepositRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DepositRepositoryAdapter implements DepositRepository {

    private final DepositJpaRepository depositJpaRepository;

    @Override
    public Optional<Deposit> findById(UUID depositId) {
        return depositJpaRepository.findById(depositId);
    }

    @Override
    public Optional<Deposit> findByUserId(UUID userId) {
        return depositJpaRepository.findByUserId(userId);
    }

    @Override
    public Deposit save(Deposit deposit) {
        return depositJpaRepository.save(deposit);
    }
}
