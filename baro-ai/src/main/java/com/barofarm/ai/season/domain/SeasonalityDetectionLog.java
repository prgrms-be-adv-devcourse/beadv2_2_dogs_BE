package com.barofarm.ai.season.domain;

import com.barofarm.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 제철 판단 로그 엔티티
 * 제철 판단 작업의 성공/실패 여부와 상세 정보를 저장
 */
@Entity
@Table(name = "seasonality_detection_log")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SeasonalityDetectionLog extends BaseEntity {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "product_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID productId;

    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;

    @Column(name = "product_category", length = 50)
    private String productCategory;

    @Column(name = "detected_seasonality_type", length = 20)
    @Enumerated(EnumType.STRING)
    private SeasonalityType detectedType;

    @Column(name = "detected_seasonality_value", length = 50)
    private String detectedValue;

    @Column(name = "confidence")
    private Double confidence;  // 0.0 ~ 1.0

    @Column(name = "llm_response", columnDefinition = "TEXT")
    private String llmResponse;  // 원본 LLM 응답 저장 (디버깅용)

    @Column(name = "reasoning", columnDefinition = "TEXT")
    private String reasoning;  // 판단 근거

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private DetectionStatus status;

    /**
     * 제철 판단 상태
     */
    public enum DetectionStatus {
        SUCCESS,  // 성공적으로 판단됨
        FAILED,   // 판단 실패 (신뢰도 낮음 또는 예외 발생)
        SKIPPED   // 판단 건너뜀 (예: 이미 제철 정보가 있음)
    }
}
