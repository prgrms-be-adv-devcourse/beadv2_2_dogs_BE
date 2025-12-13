package com.barofarm.buyer.inventory.presentation.dto;

import com.barofarm.buyer.inventory.application.dto.request.InventoryDecreaseCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record InventoryDecreaseRequest(
    @NotEmpty(message = "재고 차감 대상 상품 목록은 비어 있을 수 없습니다.")
    List<@Valid Item> items
) {
    public record Item(
        @NotNull(message = "productId는 필수 값입니다.")
        UUID productId,

        @Positive(message = "차감 수량은 0보다 큰 값이어야 합니다.")
        int quantity
    ) {}

    public InventoryDecreaseCommand toCommand() {
        return new InventoryDecreaseCommand(
            items.stream()
                .map(item -> new InventoryDecreaseCommand.Item(
                    item.productId(),
                    item.quantity()
                ))
                .collect(Collectors.toList())
        );
    }
}
