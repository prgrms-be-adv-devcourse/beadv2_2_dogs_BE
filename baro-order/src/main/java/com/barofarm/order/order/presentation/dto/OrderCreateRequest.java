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

    @NotBlank(message = "ë°›ëŠ” ë¶??´ë¦„?€ ?„ìˆ˜?…ë‹ˆ??")
    String receiverName,

    @NotBlank(message = "?´ë???ë²ˆí˜¸???„ìˆ˜?…ë‹ˆ??")
    String phone,

    @NotBlank(message = "?´ë©”?¼ì? ?„ìˆ˜?…ë‹ˆ??")
    @Email(message = "?¬ë°”ë¥??´ë©”???•ì‹???„ë‹™?ˆë‹¤.")
    String email,

    @NotBlank(message = "?°íŽ¸ë²ˆí˜¸???„ìˆ˜?…ë‹ˆ??")
    String zipCode,

    @NotBlank(message = "ì£¼ì†Œ???„ìˆ˜?…ë‹ˆ??")
    String address,

    @NotBlank(message = "?ì„¸ì£¼ì†Œ???„ìˆ˜?…ë‹ˆ??")
    String addressDetail,

    String deliveryMemo,

    @NotEmpty(message = "ì£¼ë¬¸ ?í’ˆ?€ ìµœì†Œ 1ê°??´ìƒ?´ì–´???©ë‹ˆ??")
    @Valid
    List<OrderItemRequest> items

) {
    public OrderCreateCommand toCommand() {
        List<OrderCreateCommand.OrderItemCreateCommand> itemCommands = items.stream()
            .map(i -> new OrderCreateCommand.OrderItemCreateCommand(
                i.productId(),
                i.productName,
                i.categoryName(),
                i.inventoryId,
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
        @NotNull(message = "?í’ˆ ID???„ìˆ˜?…ë‹ˆ??")
        UUID productId,

        @NotBlank(message = "?í’ˆ ?´ë¦„?€ ?„ìˆ˜?…ë‹ˆ??")
        String productName,

        String categoryName,

        @Positive(message = "?˜ëŸ‰?€ 1ê°??´ìƒ?´ì–´???©ë‹ˆ??")
        Long quantity,

        @NotNull(message = "?¬ê³  ID???„ìˆ˜?…ë‹ˆ??")
        UUID inventoryId,

        @NotNull(message = "?ë§¤??ID???„ìˆ˜?…ë‹ˆ??")
        UUID sellerId,

        @Positive(message = "?¨ê?(unitPrice)??0ë³´ë‹¤ ì»¤ì•¼ ?©ë‹ˆ??")
        long unitPrice
    ) { }
}
