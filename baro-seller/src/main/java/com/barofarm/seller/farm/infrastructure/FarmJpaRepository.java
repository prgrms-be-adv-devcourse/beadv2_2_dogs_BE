package com.barofarm.seller.farm.infrastructure;

import com.barofarm.seller.farm.domain.Farm;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmJpaRepository extends JpaRepository<Farm, UUID> {
}
