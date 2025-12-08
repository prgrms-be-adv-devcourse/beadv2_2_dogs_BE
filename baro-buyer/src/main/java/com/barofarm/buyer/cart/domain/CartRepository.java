package com.barofarm.buyer.cart.domain;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository {

  Cart save(Cart cart);

  Optional<Cart> findById(UUID cartId);

  Optional<Cart> findByBuyerId(UUID buyerId);
}
