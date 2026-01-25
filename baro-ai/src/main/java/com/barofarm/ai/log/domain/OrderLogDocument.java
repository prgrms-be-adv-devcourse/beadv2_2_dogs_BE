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
@Document(indexName = "order_event_logs")
@NoArgsConstructor
public class OrderLogDocument {

    @Id
    private String id; // Elasticsearch에서는 String ID 사용

    // 개인화 추천의 핵심 축: 누가 (userId)
    @Field(type = FieldType.Keyword)
    private UUID userId;

    // 개인화 추천의 핵심 축: 무엇에 (productId)
    @Field(type = FieldType.Keyword)
    private UUID productId;

    // 임베딩용 텍스트 데이터 - 검색 가능하도록 Text 타입
    @Field(type = FieldType.Text, analyzer = "nori")
    private String productName;

    @Field(type = FieldType.Keyword)
    private String categoryCode;

    // 이벤트 타입: ORDER_CREATED, ORDER_CANCELLED
    @Field(type = FieldType.Keyword)
    private String eventType;

    // 구매 수량 (재구매 지표)
    @Field(type = FieldType.Integer)
    private Integer quantity;

    // 시간 가중치 계산용
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Instant occurredAt;

    @Builder
    public OrderLogDocument(UUID userId,
                            UUID productId,
                            String productName,
                            String categoryCode,
                            String eventType,
                            Integer quantity,
                            Instant occurredAt) {
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.categoryCode = categoryCode;
        this.eventType = eventType;
        this.quantity = quantity;
        this.occurredAt = occurredAt;
    }
}
