package com.barofarm.ai.search.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

@Getter
@Document(indexName = "product")
public class ProductDocument {
    @Id
    private UUID productId; // 검색 결과 식별용

    // productName: nori analyzer로 토큰화 (일반 검색용)
    // productName.raw: keyword 타입으로 원본 텍스트 저장 (fuzzy search용)
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "nori"),
        otherFields = {
            @InnerField(
                suffix = "raw",
                type = FieldType.Keyword
            )
        }
    )
    private String productName; // 검색 대상

    @Field(type = FieldType.Keyword)
    private UUID productCategoryId; // 카테고리 ID

    @Field(type = FieldType.Keyword)
    private String productCategoryCode; // 카테고리 코드

    @Field(type = FieldType.Keyword)
    private String productCategoryName; // 카테고리 이름

    @Field(type = FieldType.Long)
    private Long price; // 필터링 & 정렬 기능이 필요하다면 유효 (가격대 필터, 가격순 정렬)

    @Field(type = FieldType.Keyword)
    private String status; // 검색 결과 반환 로직 내에서 ON_SALE, DISCOUNTED 상태만 결과에 노출

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    @JsonFormat(pattern = "uuuu-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    private Instant updatedAt; // 검색 결과 반환 로직 내에서 최신순으로 정렬

    @Field(type = FieldType.Dense_Vector, dims = 1536)
    private float[] vector; // OpenAI 임베딩 벡터 (1536차원)

    public ProductDocument() {
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public ProductDocument(
        UUID productId,
        String productName,
        UUID productCategoryId,
        String productCategoryCode,
        String productCategoryName,
        Long price,
        String status,
        Instant updatedAt,
        float[] vector) {
        this.productId = productId;
        this.productName = productName;
        this.productCategoryId = productCategoryId;
        this.productCategoryCode = productCategoryCode;
        this.productCategoryName = productCategoryName;
        this.price = price;
        this.status = status;
        this.updatedAt = updatedAt;
        this.vector = vector;
    }
}
