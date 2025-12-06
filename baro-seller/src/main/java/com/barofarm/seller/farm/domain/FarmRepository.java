package com.barofarm.seller.farm.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface FarmRepository {

    Farm save(Farm farm);
    Optional<Farm> findById(UUID id);
    void deleteById(UUID id);
    Page<Farm> findAll(Pageable pageable);
}
