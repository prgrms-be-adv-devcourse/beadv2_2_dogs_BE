package com.barofarm.buyer.product.domain;

import com.barofarm.buyer.common.entity.BaseEntity;
import com.barofarm.buyer.common.exception.CustomException;
import com.barofarm.buyer.product.exception.ProductErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseEntity {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(nullable = false, columnDefinition = "BINARY(16)")
  private UUID sellerId;

  @Column(name = "product_name", nullable = false, length = 50)
  private String productName;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false, length = 30)
  private ProductCategory productCategory;

  @Column(nullable = false)
  private Long price;

  @Column(name = "stock_quantity", nullable = false)
  private Integer stockQuantity;

  @Enumerated(EnumType.STRING)
  @Column(name = "product_status", nullable = false)
  private ProductStatus productStatus;

    @OneToMany(
        mappedBy = "product",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("sortOrder ASC")
    private final List<ProductImage> images = new ArrayList<>();

  private Product(
      UUID sellerId,
      String productName,
      String description,
      ProductCategory productCategory,
      Long price,
      Integer stockQuantity,
      ProductStatus productStatus) {

    validateConstructorParams(sellerId, productName, productCategory, price, stockQuantity);
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
      Long price,
      Integer stockQuantity,
      ProductStatus productStatus) {

      return new Product(
          sellerId,
          productName,
          description,
          productCategory,
          price,
          stockQuantity,
          productStatus
      );
  }

  public void update(
      String productName,
      String description,
      ProductCategory productCategory,
      Long price,
      Integer stockQuantity,
      ProductStatus productStatus) {

    validateUpdateParams(productName, productCategory, price, stockQuantity, productStatus);

    this.productName = productName;
    this.description = description;
    this.productCategory = productCategory;
    this.price = price;
    this.stockQuantity = stockQuantity;
    this.productStatus = productStatus;
  }

    public void addImage(String imageUrl, int order) {
        this.images.add(ProductImage.create(this, imageUrl, order));
    }

    public void clearImages() {
        this.images.clear();
    }

    public void replaceImages(List<String> imageUrls) {
        this.images.clear();

        int order = 0;
        for (String imageUrl : imageUrls) {
            addImage(imageUrl, order++);
        }
    }

  public void validateOwner(UUID memberId) {
    if (!this.sellerId.equals(memberId)) {
      throw new CustomException(ProductErrorCode.FORBIDDEN_NOT_PRODUCT_OWNER);
    }
  }

  private void validateConstructorParams(
      UUID sellerId, String productName, ProductCategory category, Long price, Integer stock) {
    if (sellerId == null) {
      throw new CustomException(ProductErrorCode.SELLER_NULL);
    }
    validateCommonFields(productName, category, price, stock);
  }

  private void validateUpdateParams(
      String productName,
      ProductCategory category,
      Long price,
      Integer stock,
      ProductStatus status) {
    validateCommonFields(productName, category, price, stock);
    if (status == null) {
      throw new CustomException(ProductErrorCode.STATUS_NULL);
    }
  }

  private void validateCommonFields(
      String productName, ProductCategory category, Long price, Integer stock) {
    if (productName == null) {
      throw new CustomException(ProductErrorCode.PRODUCT_NAME_NULL);
    }
    if (productName.isBlank()) {
      throw new CustomException(ProductErrorCode.PRODUCT_NAME_EMPTY);
    }
    if (productName.length() > 50) {
      throw new CustomException(ProductErrorCode.PRODUCT_NAME_TOO_LONG);
    }
    if (category == null) {
      throw new CustomException(ProductErrorCode.CATEGORY_NULL);
    }
    if (price == null) {
      throw new CustomException(ProductErrorCode.PRICE_NULL);
    }
    if (price < 0) {
      throw new CustomException(ProductErrorCode.INVALID_PRICE);
    }
    if (stock == null) {
      throw new CustomException(ProductErrorCode.STOCK_NULL);
    }
    if (stock < 0) {
      throw new CustomException(ProductErrorCode.INVALID_STOCK);
    }
  }
}
