package com.barofarm.ai.ranking.domain;

import java.util.UUID;

/**
 * 상품 랭킹 점수 도메인 모델
 */
public class ProductRankingScore {
    private UUID productId;
    private String productName;
    private Long clickCount;
    private Long totalDwellTimeMs;
    private Long dwellTimeCount; // 체류 시간 로그 개수
    private Long averageDwellTimeMs;
    private Double score; // LLM이 계산한 점수

    // TODO: 향후 추가 예정 필드
    private Long totalSalesCount; // 누적 판매량
    private Long likeCount; // 좋아요 수

    public ProductRankingScore(UUID productId, String productName) {
        this.productId = productId;
        this.productName = productName;
        this.clickCount = 0L;
        this.totalDwellTimeMs = 0L;
        this.dwellTimeCount = 0L;
        this.averageDwellTimeMs = 0L;
        this.score = 0.0;
        this.totalSalesCount = 0L;
        this.likeCount = 0L;
    }

    public void incrementClick() {
        this.clickCount++;
    }

    public void addDwellTime(long dwellTimeMs) {
        this.totalDwellTimeMs += dwellTimeMs;
        this.dwellTimeCount++;
        // 평균 체류 시간 재계산
        if (dwellTimeCount > 0) {
            this.averageDwellTimeMs = totalDwellTimeMs / dwellTimeCount;
        }
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public Long getTotalDwellTimeMs() {
        return totalDwellTimeMs;
    }

    public Long getAverageDwellTimeMs() {
        return averageDwellTimeMs;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Long getTotalSalesCount() {
        return totalSalesCount;
    }

    public void setTotalSalesCount(Long totalSalesCount) {
        this.totalSalesCount = totalSalesCount;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
}
