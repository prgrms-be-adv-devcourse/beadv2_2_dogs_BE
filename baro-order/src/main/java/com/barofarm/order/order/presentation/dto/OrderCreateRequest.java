package com.barofarm.order.order.presentation.dto;

import com.barofarm.order.order.application.dto.request.OrderCreateCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;

public record OrderCreateRequest(

    @NotBlank(message = "받는 분 이름은 필수입니다.")
    String receiverName,

    @NotBlank(message = "전화번호는 필수입니다.")
    String phone,

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @NotBlank(message = "우편번호는 필수입니다.")
    String zipCode,

    @NotBlank(message = "주소는 필수입니다.")
    String address,

    @NotBlank(message = "상세주소는 필수입니다.")
    String addressDetail,

    String deliveryMemo,

    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    @Valid
    List<OrderItemRequest> items

) {
    public OrderCreateCommand toCommand() {
        List<OrderCreateCommand.OrderItemCreateCommand> itemCommands = items.stream()
            .map(i -> new OrderCreateCommand.OrderItemCreateCommand(
                i.productId(),
                i.productName(),
                i.categoryCode(),
                i.inventoryId(),
                i.sellerId(),
                i.quantity(),
                i.unitPrice()
            ))
            .toList();

        return new OrderCreateCommand(
            receiverName,
            phone,
            email,
            zipCode,
            address,
            addressDetail,
            deliveryMemo,
            itemCommands
        );
    }

    public record OrderItemRequest(
        @NotNull(message = "상품 ID는 필수입니다.")
        UUID productId,

        @NotBlank(message = "상품 이름은 필수입니다.")
        String productName,

        String categoryCode,

        @Positive(message = "수량은 1개 이상이어야 합니다.")
        Long quantity,

        @NotNull(message = "재고 ID는 필수입니다.")
        UUID inventoryId,

        @NotNull(message = "판매자 ID는 필수입니다.")
        UUID sellerId,

        @Positive(message = "단가(unitPrice)는 0보다 커야 합니다.")
        long unitPrice
    ) { }
}
