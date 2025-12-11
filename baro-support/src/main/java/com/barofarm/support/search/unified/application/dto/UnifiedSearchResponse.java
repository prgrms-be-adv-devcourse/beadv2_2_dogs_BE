package com.barofarm.support.search.unified.application.dto;

import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.search.experience.application.dto.ExperienceSearchItem;
import com.barofarm.support.search.farm.application.dto.FarmSearchItem;
import com.barofarm.support.search.product.application.dto.ProductSearchItem;

// 통합 검색 응답 DTO
public record UnifiedSearchResponse(
    CustomPage<ProductSearchItem> products,
    CustomPage<FarmSearchItem> farms,
    CustomPage<ExperienceSearchItem> experiences) {
}
