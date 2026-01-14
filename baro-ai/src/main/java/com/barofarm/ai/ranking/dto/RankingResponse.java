package com.barofarm.ai.ranking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "랭킹 응답 DTO")
public record RankingResponse(
    @Schema(description = "Top 3 랭킹 목록")
    @JsonProperty("rankings")
    List<ProductRanking> rankings,

    @Schema(description = "분석된 시간대 정보")
    @JsonProperty("analyzedPeriod")
    AnalyzedPeriod analyzedPeriod
) {
    @Schema(description = "상품 랭킹 정보")
    public record ProductRanking(
        @Schema(description = "순위 (1~3)")
        @JsonProperty("rank")
        Integer rank,

        @Schema(description = "상품 ID")
        @JsonProperty("productId")
        UUID productId,

        @Schema(description = "상품명")
        @JsonProperty("productName")
        String productName,

        @Schema(description = "랭킹 점수 (0~100)")
        @JsonProperty("score")
        Double score,

        @Schema(description = "클릭 횟수")
        @JsonProperty("clickCount")
        Long clickCount,

        @Schema(description = "평균 체류 시간 (밀리초)")
        @JsonProperty("averageDwellTimeMs")
        Long averageDwellTimeMs
    ) {
    }

    @Schema(description = "분석된 시간대 정보")
    public record AnalyzedPeriod(
        @Schema(description = "날짜 (YYYY-MM-DD)")
        @JsonProperty("date")
        String date,

        @Schema(description = "시간 (0~23)")
        @JsonProperty("hour")
        Integer hour
    ) {
    }
}
