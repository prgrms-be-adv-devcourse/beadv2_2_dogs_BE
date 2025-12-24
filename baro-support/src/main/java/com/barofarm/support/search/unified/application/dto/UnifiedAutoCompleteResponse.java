package com.barofarm.support.search.unified.application.dto;

import com.barofarm.support.search.experience.application.dto.ExperienceAutoItem;
import com.barofarm.support.search.product.application.dto.ProductAutoItem;
import java.util.List;

// 통합 자동완성 응답 DTO
public record UnifiedAutoCompleteResponse(
    List<ProductAutoItem> products,
    List<ExperienceAutoItem> experiences
) { }
