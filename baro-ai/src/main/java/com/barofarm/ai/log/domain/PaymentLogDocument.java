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
@Document(indexName = "payment_event_logs")
@NoArgsConstructor
public class PaymentLogDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private UUID userId;

    @Field(type = FieldType.Keyword)
    private UUID paymentId;

    @Field(type = FieldType.Keyword)
    private UUID orderId;

    @Field(type = FieldType.Long)
    private Long amount;

    @Field(type = FieldType.Keyword)
    private String purpose;

    @Field(type = FieldType.Keyword)
    private String eventType;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Instant occurredAt;

    @Builder
    public PaymentLogDocument(UUID userId,
                              UUID paymentId,
                              UUID orderId,
                              Long amount,
                              String purpose,
                              String eventType,
                              Instant occurredAt) {
        this.userId = userId;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.purpose = purpose;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
    }
}

