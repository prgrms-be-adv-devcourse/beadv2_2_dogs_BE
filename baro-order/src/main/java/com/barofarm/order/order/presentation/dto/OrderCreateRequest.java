package com.barofarm.order.order.presentation.dto;

import com.barofarm.order.order.application.dto.request.OrderCreateCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;

public record OrderCreateRequest(

    @NotBlank(message = "배송지 주소는 필수입니다.")
    String address,

    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    @Valid
    List<OrderItemRequest> items

) {
    public OrderCreateCommand toCommand() {
        List<OrderCreateCommand.OrderItemCreateCommand> itemCommands = items.stream()
            .map(i -> new OrderCreateCommand.OrderItemCreateCommand(
                i.productId(),
                i.quantity(),
                i.unitPrice()
            ))
            .toList();

        return new OrderCreateCommand(
            address,
            itemCommands
        );
    }

    public record OrderItemRequest(
        @NotNull(message = "상품 ID는 필수입니다.")
        UUID productId,

        @Positive(message = "수량은 1개 이상이어야 합니다.")
        int quantity,

        @Positive(message = "단가(unitPrice)는 0보다 커야 합니다.")
        long unitPrice
    ) { }
}
