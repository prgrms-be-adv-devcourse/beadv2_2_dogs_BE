package com.barofarm.buyer.product.presentation.dto;

import com.barofarm.buyer.product.application.dto.ProductUpdateCommand;
import com.barofarm.buyer.product.domain.ProductCategory;
import com.barofarm.buyer.product.domain.ProductStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ProductUpdateRequest(
    @NotBlank(message = "상품명은 필수입니다.") String productName,
    String description,
    @NotNull(message = "카테고리는 필수입니다.") ProductCategory productCategory,
    @NotNull(message = "가격은 필수입니다.") @Min(value = 0, message = "가격은 0 이상이어야 합니다.") Long price,
    @NotNull(message = "재고는 필수입니다.") @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
        Integer stockQuantity,
    @NotNull(message = "상품 상태는 필수입니다.") ProductStatus productStatus) {
  public ProductUpdateCommand toCommand(UUID memberId, String role) {
    return new ProductUpdateCommand(
        memberId,
        role,
        productName,
        description,
        productCategory,
        price,
        stockQuantity,
        productStatus);
  }
}
