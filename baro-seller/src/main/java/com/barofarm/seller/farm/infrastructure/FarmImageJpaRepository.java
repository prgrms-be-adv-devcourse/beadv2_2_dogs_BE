package com.barofarm.seller.farm.infrastructure;

import com.barofarm.seller.farm.domain.FarmImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FarmImageJpaRepository extends JpaRepository<FarmImage, UUID> {
}
