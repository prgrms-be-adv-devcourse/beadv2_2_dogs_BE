package com.barofarm.buyer.product.presentation.dto;

import com.barofarm.buyer.product.application.dto.ProductImageUpdateMode;
import com.barofarm.buyer.product.application.dto.ProductUpdateCommand;
import com.barofarm.buyer.product.domain.ProductStatus;
import com.barofarm.buyer.product.domain.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record ProductUpdateRequest(
    @NotBlank(message = "상품명은 필수입니다.") String productName,
    String description,
    @NotNull(message = "카테고리는 필수입니다.") UUID categoryId,
    @NotNull(message = "가격은 필수입니다.") @Min(value = 0, message = "가격은 0 이상이어야 합니다.") Long price,
    @NotNull(message = "옵션은 필수입니다.")
    @Size(min = 1, message = "옵션은 최소 1개 이상이어야 합니다.")
    @Valid
        List<ProductInventoryOptionRequest> inventoryOptions,
    @NotNull(message = "상품 상태는 필수입니다.") ProductStatus productStatus,
    @Schema(description = "이미지 변경 방식: KEEP(변경 없음), REPLACE(교체), CLEAR(전부 삭제)")
    ProductImageUpdateMode imageUpdateMode) {
  public ProductUpdateCommand toCommand(UUID memberId, UserType role) {
    return new ProductUpdateCommand(
        memberId,
        role,
        productName,
        description,
        categoryId,
        price,
        inventoryOptions == null
            ? List.of()
            : inventoryOptions.stream().map(ProductInventoryOptionRequest::toCommand).toList(),
        productStatus);
  }
}
