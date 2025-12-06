package com.barofarm.seller.seller.infrastructure;

import com.barofarm.seller.seller.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SellerJpaRepository extends JpaRepository<Seller, UUID> {
}
