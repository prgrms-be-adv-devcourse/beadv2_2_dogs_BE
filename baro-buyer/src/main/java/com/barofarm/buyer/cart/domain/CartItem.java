package com.barofarm.buyer.cart.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Schema(description = "장바구니 항목 정보")
@Getter
@Entity
@Table(name = "cart_item")
public class CartItem {

  @Schema(description = "장바구니 항목 UUID")
  @Id
  private UUID id;

  @Schema(description = "소속 장바구니")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false)
  private Cart cart;

  @Schema(description = "상품 UUID")
  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Schema(description = "수량")
  @Column(nullable = false)
  private Integer quantity;

  @Schema(description = "단가")
  @Column(name = "unit_price", nullable = false)
  private Long unitPrice;

  @Schema(description = "장바구니 항목의 옵션 정보를 JSON 형태로 저장")
  @Column(name = "option_info_json", nullable = false)
  private String optionInfoJson;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public CartItem() {}

  /* ====== 정적 팩토리 메소드 ====== */

  /** 새 장바구니 항목 생성 */
  public static CartItem create(UUID productId, Integer quantity, Long unitPrice, String optionInfoJson) {
    CartItem item = new CartItem();
    item.id = UUID.randomUUID();
    item.productId = productId;
    item.quantity = quantity;
    item.unitPrice = unitPrice;
    item.optionInfoJson = optionInfoJson;
    item.createdAt = LocalDateTime.now();
    item.updatedAt = LocalDateTime.now();
    return item;
  }

  /* ====== 비즈니스 메소드 ====== */

  /** 수량 증가 */
  public void increaseQuantity(int qty) {
    if (qty <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    this.quantity += qty;
    touch();
  }

  /** 수량 변경 */
  public void changeQuantity(int qty) {
    if (qty <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    this.quantity = qty;
    touch();
  }

  /** 옵션 변경 */
  public void changeOption(String optionInfoJson) {
    this.optionInfoJson = optionInfoJson;
    touch();
  }

  /** 단일 CartItem 금액 계산 */
  public Long calculatePrice() {
    return unitPrice * quantity;
  }

  /** 상품 UUID + 옵션 JSON이 동일한지 비교 */
  public boolean isSameProductAndOption(CartItem other) {
    return this.productId.equals(other.productId)
        && Objects.equals(this.optionInfoJson, other.optionInfoJson);
  }

  /* ====== Cart와의 연관관계 관리 ====== */

  public void bindToCart(Cart cart) {
    this.cart = cart;
  }

  private void touch() {
    this.updatedAt = LocalDateTime.now();
  }
}
