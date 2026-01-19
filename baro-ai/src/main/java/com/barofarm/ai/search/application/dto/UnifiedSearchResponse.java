package com.barofarm.ai.search.application.dto;

import com.barofarm.ai.search.application.dto.experience.ExperienceSearchResponse;
import com.barofarm.ai.search.application.dto.product.ProductSearchResponse;
import com.barofarm.dto.CustomPage;

// 통합 검색 응답 DTO
public record UnifiedSearchResponse(
    CustomPage<ProductSearchResponse> products,
    CustomPage<ExperienceSearchResponse> experiences) {
}
