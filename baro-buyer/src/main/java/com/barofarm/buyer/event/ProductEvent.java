package com.barofarm.buyer.event;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductEvent {

    private ProductEventType type;
    private ProductEventData data;

    public enum ProductEventType {
        PRODUCT_CREATED,
        PRODUCT_UPDATED,
        PRODUCT_DELETED
    }

    @Getter
    @Builder
    public static class ProductEventData {
        private UUID productId;
        private String productName;
        private String productCategory;
        private Long price;
        private String status;
        private Instant updatedAt;
    }
}
