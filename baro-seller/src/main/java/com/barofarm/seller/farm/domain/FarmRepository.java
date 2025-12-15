package com.barofarm.seller.farm.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FarmRepository {

    Farm save(Farm farm);
    Optional<Farm> findById(UUID id);
    void delete(Farm farm);
    Page<Farm> findAll(Pageable pageable);
}
