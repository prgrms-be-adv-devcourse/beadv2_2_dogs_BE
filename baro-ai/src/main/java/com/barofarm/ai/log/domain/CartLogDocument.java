package com.barofarm.ai.log.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Document(indexName = "cart_event_logs")
@NoArgsConstructor
public class CartLogDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private UUID userId;

    @Field(type = FieldType.Keyword)
    private UUID productId;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String productName;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Keyword)
    private String eventType;

    @Field(type = FieldType.Integer)
    private Integer quantity;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Instant occurredAt;

    @Builder
    public CartLogDocument(UUID userId,
                           UUID productId,
                           String productName,
                           String categoryName,
                           String eventType,
                           Integer quantity,
                           Instant occurredAt) {
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.categoryName = categoryName;
        this.eventType = eventType;
        this.quantity = quantity;
        this.occurredAt = occurredAt;
    }
}
