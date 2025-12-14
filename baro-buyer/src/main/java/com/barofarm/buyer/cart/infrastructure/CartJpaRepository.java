package com.barofarm.buyer.cart.infrastructure;

import com.barofarm.buyer.cart.domain.Cart;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<Cart, UUID> {

  Optional<Cart> findByBuyerId(UUID buyerId);

  Optional<Cart> findBySessionKey(String sessionKey);
}
