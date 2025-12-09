package com.barofarm.buyer.cart.presentation;

import com.barofarm.buyer.cart.application.CartService;
import com.barofarm.buyer.cart.application.dto.CartInfo;
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

  // TODO: 인증 구현 후 기본값 제거
  private static final String TEST_BUYER_ID = "00000000-0000-0000-0000-000000000001";
  private static final String TEST_SESSION_KEY = "00000000-0000-0000-0000-000000000001";

  private final CartService cartService;

  // TODO 결제 전 장바구니 유효성 검사 validateCart 구현

  @Operation(summary = "장바구니 조회")
  @GetMapping
  public ResponseEntity<CartInfo> getCart(
      @RequestHeader(value = "X-User-Id", defaultValue = TEST_BUYER_ID) UUID buyerId,
      @RequestHeader(value = "X-Session-Key", defaultValue = TEST_SESSION_KEY) String sessionKey
  ) {
    CartInfo cartInfo = cartService.getCart(buyerId, sessionKey);
    return ResponseEntity.ok(cartInfo);
  }

  @Operation(summary = "장바구니에 상품 추가")
  @PostMapping("/items")
  public ResponseEntity<CartInfo> addItem(
      @RequestHeader(value = "X-User-Id", defaultValue = TEST_BUYER_ID) UUID buyerId,
      @RequestHeader(value = "X-Session-Key", defaultValue = TEST_SESSION_KEY) String sessionKey,
      @RequestBody AddItemRequest request) {
    CartInfo cartInfo = cartService.addItem(buyerId, sessionKey, request.toCommand());
    return ResponseEntity.ok(cartInfo);
  }

  @Operation(summary = "장바구니 항목 수량 변경")
  @PatchMapping("/items/{itemId}/quantity")
  public ResponseEntity<CartInfo> updateQuantity(
      @RequestHeader(value = "X-User-Id", defaultValue = TEST_BUYER_ID) UUID buyerId,
      @RequestHeader(value = "X-Session-Key", defaultValue = TEST_SESSION_KEY) String sessionKey,
      @PathVariable UUID itemId,
      @RequestBody UpdateQuantityRequest request) {
    CartInfo cartInfo = cartService.updateQuantity(buyerId, sessionKey, itemId, request.quantity());
    return ResponseEntity.ok(cartInfo);
  }

  @Operation(summary = "장바구니 항목 옵션 변경")
  @PatchMapping("/items/{itemId}/option")
  public ResponseEntity<CartInfo> updateOption(
      @RequestHeader(value = "X-User-Id", defaultValue = TEST_BUYER_ID) UUID buyerId,
      @RequestHeader(value = "X-Session-Key", defaultValue = TEST_SESSION_KEY) String sessionKey,
      @PathVariable UUID itemId,
      @RequestBody UpdateOptionRequest request) {
    CartInfo cartInfo = cartService.updateOption(buyerId, sessionKey, itemId, request.optionInfoJson());
    return ResponseEntity.ok(cartInfo);
  }

  @Operation(summary = "장바구니 항목 삭제")
  @DeleteMapping("/items/{itemId}")
  public ResponseEntity<Void> removeItem(
      @RequestHeader(value = "X-User-Id", defaultValue = TEST_BUYER_ID) UUID buyerId,
      @RequestHeader(value = "X-Session-Key", defaultValue = TEST_SESSION_KEY) String sessionKey,
      @PathVariable UUID itemId) {
    cartService.removeItem(buyerId, sessionKey, itemId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "장바구니 비우기")
  @DeleteMapping
  public ResponseEntity<Void> clearCart(
      @RequestHeader(value = "X-User-Id", defaultValue = TEST_BUYER_ID) UUID buyerId,
      @RequestHeader(value = "X-Session-Key", defaultValue = TEST_SESSION_KEY) String sessionKey
  ) {
    cartService.clearCart(buyerId, sessionKey);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "비로그인 장바구니 -> 로그인 사용자 장바구니 병합")
    @PostMapping("/merge")
    public ResponseEntity<CartInfo> mergeCart(
        @RequestHeader(value = "X-User-Id", defaultValue = TEST_BUYER_ID) UUID buyerId,
        @RequestHeader(value = "X-Session-Key", defaultValue = TEST_SESSION_KEY) String sessionKey
  ) {
        CartInfo cartInfo = cartService.mergeCart(buyerId, sessionKey);
        return ResponseEntity.ok(cartInfo);
    }
}
