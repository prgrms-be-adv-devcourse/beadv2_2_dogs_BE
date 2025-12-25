package com.barofarm.support.search.product.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Document(indexName = "product")
public class ProductDocument {
  @Id private UUID productId; // 검색 결과 식별용

  @Field(type = FieldType.Text, analyzer = "nori")
  private String productName; // 검색 대상

  @Field(type = FieldType.Keyword)
  private String productCategory; // 필터링 기능이 필요하다면 유효

  @Field(type = FieldType.Long)
  private Long price; // 필터링 & 정렬 기능이 필요하다면 유효 (가격대 필터, 가격순 정렬)

  @Field(type = FieldType.Keyword)
  private String status; // 검색 결과 반환 로직 내에서 ON_SALE, DISCOUNTED 상태만 결과에 노출

  @Field(type = FieldType.Date, format = DateFormat.date_time)
  @JsonFormat(pattern = "uuuu-MM-dd'T'HH:mm:ssX", timezone = "UTC")
  private Instant updatedAt; // 검색 결과 반환 로직 내에서 최신순으로 정렬

  public ProductDocument() {}

  public ProductDocument(
      UUID productId,
      String productName,
      String productCategory,
      Long price,
      String status,
      Instant updatedAt) {
    this.productId = productId;
    this.productName = productName;
    this.productCategory = productCategory;
    this.price = price;
    this.status = status;
    this.updatedAt = updatedAt;
  }
}
