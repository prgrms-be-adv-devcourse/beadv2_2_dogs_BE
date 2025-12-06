package com.barofarm.buyer.product.infrastructure;

import com.barofarm.buyer.product.domain.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {}
