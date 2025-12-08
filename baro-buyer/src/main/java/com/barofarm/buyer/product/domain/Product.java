package com.barofarm.buyer.product.domain;

import com.barofarm.buyer.common.entity.BaseEntity;
import com.barofarm.buyer.common.exception.CustomException;
import com.barofarm.buyer.product.exception.FarmErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")
@NoArgsConstructor
@Getter
public class Product extends BaseEntity {

  @Id private UUID id;

  @Column(nullable = false, columnDefinition = "BINARY(16)")
  private UUID sellerId;

  @Column(name = "product_name", nullable = false, length = 50)
  private String productName;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false, length = 30)
  private ProductCategory productCategory;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "stock_quantity", nullable = false)
  private Integer stockQuantity;

  @Enumerated(EnumType.STRING)
  @Column(name = "product_status", nullable = false)
  private ProductStatus productStatus;

  @Builder
  private Product(
      UUID sellerId,
      String productName,
      String description,
      ProductCategory productCategory,
      BigDecimal price,
      Integer stockQuantity,
      ProductStatus productStatus) {
    this.id = UUID.randomUUID();
    this.sellerId = sellerId;
    this.productName = productName;
    this.description = description;
    this.productCategory = productCategory;
    this.price = price;
    this.stockQuantity = stockQuantity;
    this.productStatus = productStatus;
  }

  public static Product create(
      UUID sellerId,
      String productName,
      String description,
      ProductCategory productCategory,
      BigDecimal price,
      Integer stockQuantity,
      ProductStatus productStatus) {

    return Product.builder()
        .sellerId(sellerId)
        .productName(productName)
        .description(description)
        .productCategory(productCategory)
        .price(price)
        .stockQuantity(stockQuantity)
        .productStatus(productStatus)
        .build();
  }

  public void update(
      String productName,
      String description,
      ProductCategory productCategory,
      BigDecimal price,
      Integer stockQuantity,
      ProductStatus productStatus) {
    this.productName = productName;
    this.description = description;
    this.productCategory = productCategory;
    this.price = price;
    this.stockQuantity = stockQuantity;
    this.productStatus = productStatus;
  }

  public void validateOwner(UUID memberId) {
    if (!this.sellerId.equals(memberId)) {
      throw new CustomException(FarmErrorCode.FORBIDDEN_NOT_PRODUCT_OWNER);
    }
  }
}
