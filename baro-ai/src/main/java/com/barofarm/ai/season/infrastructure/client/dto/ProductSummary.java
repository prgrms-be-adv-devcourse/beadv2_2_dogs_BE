package com.barofarm.ai.season.infrastructure.client.dto;

import java.util.UUID;

/**
 * AI 서비스에서 제철 판단에 필요한 최소한의 상품 정보
 */
public record ProductSummary(
    UUID id,
    String productName,
    String categoryCode
) {
}
