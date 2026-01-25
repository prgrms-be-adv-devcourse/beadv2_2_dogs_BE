package com.barofarm.buyer.product.domain;

import com.barofarm.buyer.product.exception.ProductErrorCode;
import com.barofarm.entity.BaseEntity;
import com.barofarm.exception.CustomException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "category_id",
      nullable = false,
      foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
  )
  private Category category;

  @Column(nullable = false)
  private Long price;

  @Enumerated(EnumType.STRING)
  @Column(name = "product_status", nullable = false)
  private ProductStatus productStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "seasonality_type")
  private SeasonalityType seasonalityType;

  @Column(name = "seasonality_value", length = 20)
  private String seasonalityValue;

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
      Category category,
      Long price,
      ProductStatus productStatus) {

    validateConstructorParams(sellerId, productName, category, price);
    this.id = UUID.randomUUID();
    this.sellerId = sellerId;
    this.productName = productName;
    this.description = description;
    this.category = category;
    this.price = price;
    this.productStatus = productStatus;
  }

  public static Product create(
      UUID sellerId,
      String productName,
      String description,
      Category category,
      Long price,
      ProductStatus productStatus) {

      return new Product(
          sellerId,
          productName,
          description,
          category,
          price,
          productStatus
      );
  }

  public void update(
      String productName,
      String description,
      Category category,
      Long price,
      ProductStatus productStatus) {

    validateUpdateParams(productName, category, price, productStatus);

    this.productName = productName;
    this.description = description;
    this.category = category;
    this.price = price;
    this.productStatus = productStatus;
  }

    public void addImage(String imageUrl, int order) {
        this.images.add(ProductImage.create(this, imageUrl, null, order));
    }

    public void addImage(String imageUrl, String s3Key, int order) {
        this.images.add(ProductImage.create(this, imageUrl, s3Key, order));
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

  public void updateSeasonality(SeasonalityType seasonalityType, String seasonalityValue) {
    this.seasonalityType = seasonalityType;
    this.seasonalityValue = seasonalityValue;
  }

  public void validateOwner(UUID memberId) {
    if (!this.sellerId.equals(memberId)) {
      throw new CustomException(ProductErrorCode.FORBIDDEN_NOT_PRODUCT_OWNER);
    }
  }

  private void validateConstructorParams(
      UUID sellerId, String productName, Category category, Long price) {
    if (sellerId == null) {
      throw new CustomException(ProductErrorCode.SELLER_NULL);
    }
    validateCommonFields(productName, category, price);
  }

  private void validateUpdateParams(
      String productName,
      Category category,
      Long price,
      ProductStatus status) {
    validateCommonFields(productName, category, price);
    if (status == null) {
      throw new CustomException(ProductErrorCode.STATUS_NULL);
    }
  }

  private void validateCommonFields(
      String productName, Category category, Long price) {
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
  }
}
