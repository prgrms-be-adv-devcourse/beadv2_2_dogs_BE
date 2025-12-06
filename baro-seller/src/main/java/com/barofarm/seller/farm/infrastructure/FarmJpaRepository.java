package com.barofarm.seller.farm.infrastructure;

import com.barofarm.seller.farm.domain.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FarmJpaRepository extends JpaRepository<Farm, UUID> {
}
