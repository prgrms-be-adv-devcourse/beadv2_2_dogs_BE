package com.barofarm.support.search.farm.domain;

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
@Document(indexName = "farm")
public class FarmDocument {
  @Id private UUID farmId; // 검색 결과 식별용

  @Field(type = FieldType.Text, analyzer = "nori")
  private String farmName; // 검색 대상

  @Field(type = FieldType.Text, analyzer = "nori")
  private String farmAddress; // 농장 주소 (주소 검색/필터 대비)

  @Field(type = FieldType.Keyword)
  private String status; // 검색 결과 반환 로직 내에서 ACTIVE 상태만 결과에 노출

  @Field(type = FieldType.Date, format = DateFormat.date_time)
  @JsonFormat(pattern = "uuuu-MM-dd'T'HH:mm:ssX", timezone = "UTC")
  private Instant updatedAt; // 검색 결과 반환 로직 내에서 최신순으로 정렬

  public FarmDocument() {}

  public FarmDocument(
      UUID farmId, String farmName, String farmAddress, String status, Instant updatedAt) {
    this.farmId = farmId;
    this.farmName = farmName;
    this.farmAddress = farmAddress;
    this.status = status;
    this.updatedAt = updatedAt;
  }
}
