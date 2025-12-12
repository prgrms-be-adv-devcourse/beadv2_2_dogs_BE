package com.barofarm.order.order.presentation.dto;

import com.barofarm.order.order.application.dto.request.OrderCreateCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record OrderCreateRequest(

    @NotBlank(message = "받는 분 이름은 필수입니다.")
    String receiverName,

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    String phone,

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email,

    @NotBlank(message = "우편번호는 필수입니다.")
    String zipCode,

    @NotBlank(message = "주소는 필수입니다.")
    String address,

    @NotBlank(message = "상세주소는 필수입니다.")
    String addressDetail,

    String deliveryMemo,  // 선택값 → 검증 없음

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

        @Positive(message = "수량은 1개 이상이어야 합니다.")
        int quantity,

        @Positive(message = "단가(unitPrice)는 0보다 커야 합니다.")
        long unitPrice
    ) { }
}
