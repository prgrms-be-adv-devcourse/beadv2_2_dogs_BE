package com.barofarm.ai.embedding.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Builder
@Document(indexName = "user_profile_embeddings")
public class UserProfileEmbeddingDocument {

    // 사용자 ID를 문서의 고유 ID로 사용
    @Id
    private UUID userId;

    // OpenAI 'text-embedding-ada-002' 모델의 기본 차원인 1536으로 설정
    @Field(type = FieldType.Dense_Vector, dims = 1536)
    private List<Double> userProfileVector;

    @Field(type = FieldType.Date)
    private Instant lastUpdatedAt;

    // 임베딩 생성에 사용한 로그들의 ID (동일성/재현성 확보)
    @Field(type = FieldType.Keyword)
    private List<String> sourceSearchLogIds;  // 최대 5개

    @Field(type = FieldType.Keyword)
    private List<String> sourceCartLogIds;    // 최대 5개

    @Field(type = FieldType.Keyword)
    private List<String> sourceOrderLogIds;   // 최대 5개

    // 빠른 상품 제외를 위한 productId 목록 (cart+order에서 추출, UUID 문자열)
    @Field(type = FieldType.Keyword)
    private List<String> sourceProductIds;    // 최대 10개 (중복 제거)

    // 사용자 선호 카테고리 코드 (cart/order 로그에서 도출한 최빈 카테고리)
    @Field(type = FieldType.Keyword)
    private String preferredCategoryCode;
}
