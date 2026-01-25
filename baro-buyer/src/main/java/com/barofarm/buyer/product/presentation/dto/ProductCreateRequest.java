package com.barofarm.buyer.product.presentation.dto;

import com.barofarm.buyer.product.application.dto.ProductCreateCommand;
import com.barofarm.buyer.product.domain.ProductStatus;
import com.barofarm.buyer.product.domain.UserType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record ProductCreateRequest(
    @NotBlank(message = "상품명은 필수입니다.")
    String productName,

    String description,

    @NotNull(message = "카테고리는 필수입니다.")
    UUID categoryId,

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    @Max(value = 1_000_000_000L, message = "가격는 10억까지만 입력할 수 있습니다.")
    Long price,

    @NotNull(message = "옵션은 필수입니다.")
    @Size(min = 1, message = "옵션은 최소 1개 이상이어야 합니다.")
    @Valid
    List<ProductInventoryOptionRequest> inventoryOptions,

    @NotNull(message = "상품 상태는 필수입니다.")
    ProductStatus productStatus) {
  public ProductCreateCommand toCommand(UUID memberId, UserType role) {
    return new ProductCreateCommand(
        memberId,
        role,
        productName,
        description,
        categoryId,
        price,
        inventoryOptions == null
            ? List.of()
            : inventoryOptions.stream().map(ProductInventoryOptionRequest::toCommand).toList());
  }
}
