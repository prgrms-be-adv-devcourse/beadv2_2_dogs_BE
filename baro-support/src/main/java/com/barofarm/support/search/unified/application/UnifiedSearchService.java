package com.barofarm.support.search.unified.application;

import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.search.experience.application.ExperienceSearchService;
import com.barofarm.support.search.experience.application.dto.ExperienceSearchItem;
import com.barofarm.support.search.farm.application.FarmSearchService;
import com.barofarm.support.search.farm.application.dto.FarmSearchItem;
import com.barofarm.support.search.product.application.ProductSearchService;
import com.barofarm.support.search.product.application.dto.ProductSearchItem;
import com.barofarm.support.search.unified.application.dto.UnifiedSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnifiedSearchService {
    private final ProductSearchService productSearchService;
    private final FarmSearchService farmSearchService;
    private final ExperienceSearchService experienceSearchService;

    public UnifiedSearchResponse search(String q, Pageable pageable) {
        // 각 서비스에서 CustomPage를 직접 반환받아서 조합
        CustomPage<ProductSearchItem> products = productSearchService.searchProducts(q, pageable);
        CustomPage<FarmSearchItem> farms = farmSearchService.searchFarms(q, pageable);
        CustomPage<ExperienceSearchItem> experiences = experienceSearchService.searchExperiences(q, pageable);

        return new UnifiedSearchResponse(products, farms, experiences);
    }
}
