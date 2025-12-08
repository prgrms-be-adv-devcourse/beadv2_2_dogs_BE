package com.barofarm.buyer.cart.application;

import com.barofarm.buyer.cart.application.dto.CartCommand;
import com.barofarm.buyer.cart.application.dto.CartInfo;
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

  /** 장바구니 조회 */
  public CartInfo getCart(UUID buyerId) {
    // TODO: 구현
    return null;
  }

  /** 장바구니에 상품 추가 */
  @Transactional
  public CartInfo addItem(UUID buyerId, CartCommand.AddItem command) {
    // TODO: 구현
    return null;
  }

  /** 장바구니 항목 수량 변경 */
  @Transactional
  public CartInfo updateQuantity(UUID buyerId, UUID itemId, int quantity) {
    // TODO: 구현
    return null;
  }

  /** 장바구니 항목 옵션 변경 */
  @Transactional
  public CartInfo updateOption(UUID buyerId, UUID itemId, String optionInfoJson) {
    // TODO: 구현
    return null;
  }

  /** 장바구니 항목 삭제 */
  @Transactional
  public void removeItem(UUID buyerId, UUID itemId) {
    // TODO: 구현
  }

  /** 장바구니 비우기 */
  @Transactional
  public void clearCart(UUID buyerId) {
    // TODO: 구현
  }
}
