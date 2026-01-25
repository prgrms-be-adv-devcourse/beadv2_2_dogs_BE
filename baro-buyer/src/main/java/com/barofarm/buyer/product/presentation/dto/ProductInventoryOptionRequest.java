package com.barofarm.buyer.product.presentation.dto;

import com.barofarm.buyer.product.application.dto.ProductInventoryOptionCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductInventoryOptionRequest(
    @NotNull(message = "재고 수량은 필수입니다.")
    @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    @Max(value = 100_000_000L, message = "재고는 1억까지만 입력할 수 있습니다.")
    Long quantity,
    @NotNull(message = "단위는 필수입니다.")
    Integer unit
) {
    public ProductInventoryOptionCommand toCommand() {
        return new ProductInventoryOptionCommand(quantity, unit);
    }
}
