package com.barofarm.support.search.unified.application.dto;

import com.barofarm.support.search.experience.application.dto.ExperienceSearchResponse;
import com.barofarm.support.search.farm.application.dto.FarmSearchResponse;
import com.barofarm.support.search.product.application.dto.ProductSearchResponse;

// 통합 검색 응답 DTO
public record UnifiedSearchResponse(
    ProductSearchResponse products,
    FarmSearchResponse farms,
    ExperienceSearchResponse experiences) {}
