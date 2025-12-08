package com.barofarm.buyer.cart.infrastructure;

import com.barofarm.buyer.cart.domain.Cart;
import com.barofarm.buyer.cart.domain.CartRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** CartRepository 구현체 Domain Layer의 CartRepository 인터페이스를 구현 */
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

  @Override
  public void deleteById(UUID cartId) {
    jpaRepository.deleteById(cartId);
  }

  @Override
  public boolean existsByBuyerId(UUID buyerId) {
    return jpaRepository.existsByBuyerId(buyerId);
  }
}
