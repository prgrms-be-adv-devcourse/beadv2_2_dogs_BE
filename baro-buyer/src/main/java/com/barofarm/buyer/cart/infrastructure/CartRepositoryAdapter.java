package com.barofarm.buyer.cart.infrastructure;

import com.barofarm.buyer.cart.domain.Cart;
import com.barofarm.buyer.cart.domain.CartRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartRepository {

  private final CartJpaRepository jpaRepository;

  @Override
  public Cart save(Cart cart) {
    return jpaRepository.save(cart);
  }

  @Override
  public Optional<Cart> findById(UUID cartId) {
    return jpaRepository.findById(cartId);
  }

  @Override
  public Optional<Cart> findByBuyerId(UUID buyerId) {
    return jpaRepository.findByBuyerId(buyerId);
  }
}
