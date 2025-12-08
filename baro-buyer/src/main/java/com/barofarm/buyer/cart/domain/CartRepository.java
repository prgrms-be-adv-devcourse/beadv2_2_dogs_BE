package com.barofarm.buyer.cart.domain;

import java.util.Optional;
import java.util.UUID;

/** 장바구니 Repository 인터페이스 (Domain Layer) */
public interface CartRepository {

  Cart save(Cart cart);

  Optional<Cart> findById(UUID cartId);

  Optional<Cart> findByBuyerId(UUID buyerId);

  void deleteById(UUID cartId);

  boolean existsByBuyerId(UUID buyerId);
}
