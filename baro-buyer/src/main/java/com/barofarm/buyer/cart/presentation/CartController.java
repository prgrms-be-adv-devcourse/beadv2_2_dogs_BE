package com.barofarm.buyer.cart.presentation;

import com.barofarm.buyer.cart.application.CartService;
import com.barofarm.buyer.cart.application.dto.CartInfo;
import com.barofarm.buyer.cart.application.dto.CartValidationInfo;
import com.barofarm.buyer.cart.presentation.dto.AddItemRequest;
import com.barofarm.buyer.cart.presentation.dto.UpdateOptionRequest;
import com.barofarm.buyer.cart.presentation.dto.UpdateQuantityRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "장바구니 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class CartController {

  private final CartService cartService;

  @Operation(summary = "장바구니 조회",
      description = "로그인 사용자: X-User-Id 설정, 비로그인 사용자: X-Session-Key만 설정")
  @GetMapping
  public ResponseEntity<CartInfo> getCart(
      @RequestHeader(value = "X-User-Id", required = false) UUID buyerId,
      @RequestHeader(value = "X-Session-Key", required = false) String sessionKey
  ) {
    CartInfo cartInfo = cartService.getCart(buyerId, sessionKey);
    return ResponseEntity.ok(cartInfo);
  }

  @Operation(summary = "장바구니에 상품 추가",
      description = "로그인 사용자: X-User-Id 설정, 비로그인 사용자: X-Session-Key만 설정")
  @PostMapping("/items")
  public ResponseEntity<CartInfo> addItem(
      @RequestHeader(value = "X-User-Id", required = false) UUID buyerId,
      @RequestHeader(value = "X-Session-Key", required = false) String sessionKey,
      @RequestBody AddItemRequest request) {
    CartInfo cartInfo = cartService.addItem(buyerId, sessionKey, request.toCommand());
    return ResponseEntity.ok(cartInfo);
  }

  @Operation(summary = "장바구니 항목 수량 변경")
  @PatchMapping("/items/{itemId}/quantity")
  public ResponseEntity<CartInfo> updateQuantity(
      @RequestHeader(value = "X-User-Id", required = false) UUID buyerId,
      @RequestHeader(value = "X-Session-Key", required = false) String sessionKey,
      @PathVariable UUID itemId,
      @RequestBody UpdateQuantityRequest request) {
    CartInfo cartInfo = cartService.updateQuantity(buyerId, sessionKey, itemId, request.quantity());
    return ResponseEntity.ok(cartInfo);
  }

  @Operation(summary = "장바구니 항목 옵션 변경")
  @PatchMapping("/items/{itemId}/option")
  public ResponseEntity<CartInfo> updateOption(
      @RequestHeader(value = "X-User-Id", required = false) UUID buyerId,
      @RequestHeader(value = "X-Session-Key", required = false) String sessionKey,
      @PathVariable UUID itemId,
      @RequestBody UpdateOptionRequest request) {
    CartInfo cartInfo = cartService.updateOption(buyerId, sessionKey, itemId, request.optionInfoJson());
    return ResponseEntity.ok(cartInfo);
  }

  @Operation(summary = "장바구니 항목 삭제")
  @DeleteMapping("/items/{itemId}")
  public ResponseEntity<Void> removeItem(
      @RequestHeader(value = "X-User-Id", required = false) UUID buyerId,
      @RequestHeader(value = "X-Session-Key", required = false) String sessionKey,
      @PathVariable UUID itemId) {
    cartService.removeItem(buyerId, sessionKey, itemId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "장바구니 비우기")
  @DeleteMapping
  public ResponseEntity<Void> clearCart(
      @RequestHeader(value = "X-User-Id", required = false) UUID buyerId,
      @RequestHeader(value = "X-Session-Key", required = false) String sessionKey
  ) {
    cartService.clearCart(buyerId, sessionKey);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "비로그인 장바구니를 로그인 사용자 장바구니로 병합",
      description = "X-User-Id: 로그인한 사용자 ID, X-Session-Key: 비로그인 시 사용했던 세션 키")
  @PostMapping("/merge")
  public ResponseEntity<CartInfo> mergeCart(
      @RequestHeader(value = "X-User-Id") UUID buyerId,
      @RequestHeader(value = "X-Session-Key") String sessionKey
  ) {
    CartInfo cartInfo = cartService.mergeCart(buyerId, sessionKey);
    return ResponseEntity.ok(cartInfo);
  }

  @Operation(summary = "결제 직전 장바구니 검증")
  @PostMapping("/validate")
  public ResponseEntity<CartValidationInfo> validateForCheckout(
      @RequestHeader(value = "X-User-Id", required = false) UUID buyerId,
      @RequestHeader(value = "X-Session-Key", required = false) String sessionKey
  ) {
    CartValidationInfo response = cartService.validateForCheckout(buyerId, sessionKey);
    return ResponseEntity.ok(response);
  }
}
