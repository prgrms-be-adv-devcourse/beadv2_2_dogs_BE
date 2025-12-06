package com.barofarm.buyer.product.presentation.dto;

import com.barofarm.buyer.product.application.dto.ProductCreateCommand;
import com.barofarm.buyer.product.domain.ProductCategory;
import com.barofarm.buyer.product.domain.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductCreateRequest(
    @NotBlank(message = "상품명은 필수입니다.") String productName,
    String description,
    @NotNull(message = "카테고리는 필수입니다.") ProductCategory productCategory,
    @NotNull(message = "가격은 필수입니다.")
        @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다.")
        BigDecimal price,
    @NotNull(message = "가격은 필수입니다.") @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
        Integer stockQuantity,
    @NotNull(message = "상품 상태는 필수입니다.") ProductStatus productStatus) {
  public ProductCreateCommand toCommand(UUID memberId, String role) {
    return new ProductCreateCommand(
        memberId, role, productName, description, productCategory, price, stockQuantity);
  }
}
