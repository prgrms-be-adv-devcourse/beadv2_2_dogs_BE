package com.barofarm.log.history.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartEventData {
    private UUID cartId;
    private UUID cartItemId;
    private UUID productId;
    private String productName;
    private Integer quantity;
}
