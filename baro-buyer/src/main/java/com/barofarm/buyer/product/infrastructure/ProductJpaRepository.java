package com.barofarm.buyer.product.infrastructure;

import com.barofarm.buyer.product.domain.Product;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

    /**
     * 제철 정보가 없는 상품 목록 조회 (seasonalityType이 null인 상품)
     */
    @Query("SELECT p FROM Product p WHERE p.seasonalityType IS NULL")
    Page<Product> findAllWithoutSeasonality(Pageable pageable);
}
