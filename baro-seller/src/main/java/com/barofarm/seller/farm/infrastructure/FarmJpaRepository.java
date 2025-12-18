package com.barofarm.seller.farm.infrastructure;

import com.barofarm.seller.farm.domain.Farm;
import com.barofarm.seller.seller.domain.Seller;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmJpaRepository extends JpaRepository<Farm, UUID> {
    Page<Farm> findBySeller(Seller seller, Pageable pageable);
}
