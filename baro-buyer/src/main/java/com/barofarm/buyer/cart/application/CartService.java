package com.barofarm.buyer.cart.application;

import com.barofarm.buyer.cart.application.dto.AddItemCommand;
import com.barofarm.buyer.cart.application.dto.CartInfo;
import com.barofarm.buyer.cart.domain.Cart;
import com.barofarm.buyer.cart.domain.CartItem;
import com.barofarm.buyer.cart.domain.CartRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 장바구니 Application Service (유스케이스 구현) */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

  private final CartRepository cartRepository;

  // 장바구니 조회
  public CartInfo getCart(UUID buyerId) {
      return cartRepository.findByBuyerId(buyerId)
          .map(CartInfo::from)
          .orElseGet(CartInfo::empty);
  }

  // 장바구니에 상품 추가
  @Transactional
  public CartInfo addItem(UUID buyerId, AddItemCommand command) {
    // 1. 장바구니 조회 또는 신규 생성
    Cart cart = cartRepository.findByBuyerId(buyerId)
        .orElseGet(() -> Cart.create(buyerId));

    // 2. CartItem 생성
      CartItem item = CartItem.create(
          command.productId(),
          command.quantity(),
          command.unitPrice(),
          command.optionInfoJson()
      );

      // 도메인 로직 호출 후 저장
      cart.addItem(item);
      cartRepository.save(cart);

    return CartInfo.from(cart);
  }

  // 장바구니 항목 수량 변경
  @Transactional
  public CartInfo updateQuantity(UUID buyerId, UUID itemId, int quantity) {
    Cart cart = cartRepository.findByBuyerId(buyerId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

    cart.updateQuantity(itemId, quantity);
    cartRepository.save(cart);

    return CartInfo.from(cart);
  }

  // 장바구니 항목 옵션 변경
  @Transactional
  public CartInfo updateOption(UUID buyerId, UUID itemId, String optionInfoJson) {
    Cart cart = cartRepository.findByBuyerId(buyerId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

    cart.updateOption(itemId, optionInfoJson); // 옵션 변경 + 병합 처리 inside
    cartRepository.save(cart);

    return CartInfo.from(cart);
  }

  // 장바구니 항목 삭제
  @Transactional
  public void removeItem(UUID buyerId, UUID itemId) {
      Cart cart = cartRepository.findByBuyerId(buyerId)
          .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

      cart.removeItem(itemId);
      cartRepository.save(cart);
  }

  // 장바구니 전체 삭제
  @Transactional
  public void clearCart(UUID buyerId) {
      Cart cart = cartRepository.findByBuyerId(buyerId)
          .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

      cart.clear();
      cartRepository.save(cart);
  }
}
