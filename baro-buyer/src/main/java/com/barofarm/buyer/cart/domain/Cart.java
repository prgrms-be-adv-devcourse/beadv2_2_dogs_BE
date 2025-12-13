package com.barofarm.buyer.cart.domain;

import com.barofarm.buyer.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;

@Schema(description = "장바구니 정보")
@Getter
@Entity
@Table(name = "cart")
public class Cart extends BaseEntity {

  @Schema(description = "장바구니 UUID")
  @Id
  private UUID id;

  @Schema(description = "유저 UUID (로그인 사용자)")
  @Column(name = "buyer_id")
  private UUID buyerId;

  @Schema(description = "session key (비로그인 사용자)")
  @Column(name = "session_key")
  private String sessionKey;

  @Schema(description = "장바구니 상태 관리")
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CartStatus status; // ACTIVE, MERGED, EXPIRED

  @Schema(description = "장바구니 안의 개별 상품")
  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CartItem> items = new ArrayList<>();

  public Cart() {}

  /* ====== 정적 팩토리 메소드 ====== */

  /** 로그인 사용자용 장바구니 생성 */
  public static Cart create(UUID buyerId) {
    Cart cart = new Cart();
    cart.id = UUID.randomUUID();
    cart.buyerId = buyerId;
    cart.status = CartStatus.ACTIVE;
    cart.items = new ArrayList<>();
    return cart;
  }

  /** 비로그인 사용자용 장바구니 생성 */
  public static Cart createForGuest(String sessionKey) {
    Cart cart = new Cart();
    cart.id = UUID.randomUUID();
    cart.sessionKey = sessionKey;
    cart.status = CartStatus.ACTIVE;
    cart.items = new ArrayList<>();
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
  public boolean updateQuantity(UUID cartItemId, int newQty) {
    Optional<CartItem> opt = findItem(cartItemId);

    if (opt.isEmpty()) {
        return false;
    }
    opt.get().changeQuantity(newQty);
    touch();

    return true;
  }

  /** CartItem 옵션 변경 */
  public boolean updateOption(UUID cartItemId, String newOptionJson) {
    Optional<CartItem> opt = findItem(cartItemId);

    if (opt.isEmpty()) {
        return false;
    }

    CartItem item = opt.get();
    // 옵션을 변경했는데 이미 같은 옵션의 다른 항목이 있다면 병합
    item.changeOption(newOptionJson);
    mergeAfterOptionChange(item);
    touch();

    return true;
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

  private Optional<CartItem> findItem(UUID itemId) {
    return items.stream()
        .filter(i -> i.getId().equals(itemId))
        .findFirst();
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
    updateTimestamp();
  }
}
