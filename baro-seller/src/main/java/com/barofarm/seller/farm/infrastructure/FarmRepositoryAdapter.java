package com.barofarm.seller.farm.infrastructure;

import com.barofarm.seller.farm.domain.Farm;
import com.barofarm.seller.farm.domain.FarmRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FarmRepositoryAdapter implements FarmRepository {

    private final FarmJpaRepository farmJpaRepository;

    @Override
    public Farm save(Farm farm) {
        return farmJpaRepository.save(farm);
    }

    @Override
    public Optional<Farm> findById(UUID id) {
        return farmJpaRepository.findById(id);
    }

    @Override
    public void delete(Farm farm) {
        farmJpaRepository.delete(farm);
    }

    @Override
    public Page<Farm> findAll(Pageable pageable) {
        return farmJpaRepository.findAll(pageable);
    }
}
