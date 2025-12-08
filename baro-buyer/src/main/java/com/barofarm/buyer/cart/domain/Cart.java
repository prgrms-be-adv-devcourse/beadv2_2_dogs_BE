package com.barofarm.buyer.cart.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Schema(description = "장바구니 정보")
@Getter
@Entity
@Table(name = "cart")
public class Cart {

  @Schema(description = "장바구니 UUID")
  @Id
  private UUID id;

  @Schema(description = "유저 UUID")
  @Column(name = "buyer_id", nullable = false)
  private UUID buyerId;

  @Schema(description = "장바구니 안의 개별 상품")
  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CartItem> items = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Cart() {}

  public Cart(UUID id, UUID buyerId, List<CartItem> items) {
    this.id = id;
    this.buyerId = buyerId;
    this.items = items;
  }

  /* ====== 정적 팩토리 메소드 ====== */

  /** 새 장바구니 생성 */
  public static Cart create(UUID buyerId) {
    Cart cart = new Cart();
    cart.id = UUID.randomUUID();
    cart.buyerId = buyerId;
    cart.items = new ArrayList<>();
    cart.createdAt = LocalDateTime.now();
    cart.updatedAt = LocalDateTime.now();
    return cart;
  }

  /* ====== 비즈니스 메소드 정리 ====== */

  /** 장바구니에 상품 추가 (같은 상품+옵션이면 병합) */
  public void addItem(CartItem newItem) {
    CartItem existing = findSameItem(newItem);

    if (existing != null) {
      existing.increaseQuantity(newItem.getQuantity());
    } else {
      newItem.bindToCart(this);
      this.items.add(newItem);
    }
    touch();
  }

  /** 특정 CartItem을 삭제 */
  public void removeItem(UUID cartItemId) {
    items.removeIf(item -> item.getId().equals(cartItemId));
    touch();
  }

  /** CartItem 수량 변경 */
  public void updateQuantity(UUID cartItemId, int newQty) {
    CartItem item = findItem(cartItemId);
    item.changeQuantity(newQty);
    touch();
  }

  /** CartItem 옵션 변경 */
  public void updateOption(UUID cartItemId, String newOptionJson) {
    CartItem item = findItem(cartItemId);

    item.changeOption(newOptionJson);

    // 옵션을 변경했는데 이미 같은 옵션의 다른 항목이 있다면 병합
    mergeAfterOptionChange(item);

    touch();
  }

  /** 전체 금액 계산 */
  public Long calculateTotalPrice() {
    return items.stream().mapToLong(CartItem::calculatePrice).sum();
  }

  /** 장바구니 비우기 */
  public void clear() {
    items.clear();
    touch();
  }

  /* ====== 내부 유틸 메소드 ====== */

  private CartItem findItem(UUID itemId) {
    return items.stream()
        .filter(i -> i.getId().equals(itemId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("CartItem not found"));
  }

  private CartItem findSameItem(CartItem newItem) {
    return items.stream().filter(i -> i.isSameProductAndOption(newItem))
        .findFirst()
        .orElse(null);
  }

  /** 옵션 변경 후 병합 처리 */
  private void mergeAfterOptionChange(CartItem updatedItem) {
    items.stream()
        .filter(i -> !i.getId().equals(updatedItem.getId()))
        .filter(i -> i.isSameProductAndOption(updatedItem))
        .findFirst()
        .ifPresent(
            dup -> {
              updatedItem.increaseQuantity(dup.getQuantity());
              items.remove(dup);
            });
  }

  private void touch() {
    updatedAt = LocalDateTime.now();
  }
}
