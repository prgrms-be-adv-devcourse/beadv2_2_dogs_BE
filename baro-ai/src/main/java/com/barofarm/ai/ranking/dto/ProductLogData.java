package com.barofarm.ai.ranking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * 로그 파일에서 파싱된 상품 로그 데이터 DTO
 */
public record ProductLogData(
    @JsonProperty("productId")
    UUID productId,

    @JsonProperty("productName")
    String productName,

    @JsonProperty("dwellTimeMs")
    Long dwellTimeMs,

    @JsonProperty("timestamp")
    String timestamp,

    @JsonProperty("userId")
    String userId
) {
    /**
     * 클릭 로그인지 확인 (dwellTimeMs가 null이면 클릭 로그)
     */
    public boolean isClickLog() {
        return dwellTimeMs == null;
    }

    /**
     * 체류 시간 로그인지 확인
     */
    public boolean isDwellTimeLog() {
        return dwellTimeMs != null;
    }
}
