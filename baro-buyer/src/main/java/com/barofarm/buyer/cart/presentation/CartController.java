package com.barofarm.buyer.cart.presentation;

import com.barofarm.buyer.cart.application.CartService;
import com.barofarm.buyer.cart.presentation.dto.AddItemRequest;
import com.barofarm.buyer.cart.presentation.dto.CartResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cart", description = "장바구니 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class CartController {

  private final CartService cartService;

  @Operation(summary = "장바구니 조회")
  @GetMapping
  public ResponseEntity<CartResponse> getCart() {
    // TODO: 구현
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "장바구니에 상품 추가")
  @PostMapping("/items")
  public ResponseEntity<CartResponse> addItem(@RequestBody AddItemRequest request) {
    // TODO: 구현
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "장바구니 항목 수량 변경")
  @PatchMapping("/items/{itemId}/quantity")
  public ResponseEntity<CartResponse> updateQuantity(
      @PathVariable UUID itemId, @RequestBody UpdateQuantityRequest request) {
    // TODO: 구현
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "장바구니 항목 옵션 변경")
  @PatchMapping("/items/{itemId}/option")
  public ResponseEntity<CartResponse> updateOption(
      @PathVariable UUID itemId, @RequestBody UpdateOptionRequest request) {
    // TODO: 구현
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "장바구니 항목 삭제")
  @DeleteMapping("/items/{itemId}")
  public ResponseEntity<Void> removeItem(@PathVariable UUID itemId) {
    // TODO: 구현
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "장바구니 비우기")
  @DeleteMapping
  public ResponseEntity<Void> clearCart() {
    // TODO: 구현
    return ResponseEntity.noContent().build();
  }
}
