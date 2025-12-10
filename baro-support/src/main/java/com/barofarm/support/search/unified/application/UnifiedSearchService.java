package com.barofarm.support.search.unified.application;

import com.barofarm.support.search.experience.application.ExperienceSearchService;
import com.barofarm.support.search.experience.application.dto.ExperienceSearchResponse;
import com.barofarm.support.search.farm.application.FarmSearchService;
import com.barofarm.support.search.farm.application.dto.FarmSearchResponse;
import com.barofarm.support.search.product.application.ProductSearchService;
import com.barofarm.support.search.product.application.dto.ProductSearchResponse;
import com.barofarm.support.search.unified.application.dto.UnifiedSearchRequest;
import com.barofarm.support.search.unified.application.dto.UnifiedSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnifiedSearchService {
    private final ProductSearchService productSearchService;
    private final FarmSearchService farmSearchService;
    private final ExperienceSearchService experienceSearchService;

    // TODO 페이징 하드코딩 수정
    public UnifiedSearchResponse search(UnifiedSearchRequest request) {
        ProductSearchResponse products =
            productSearchService.searchProducts(request.q(), PageRequest.of(0, 10));
        FarmSearchResponse farms =
            farmSearchService.searchFarms(request.q(), PageRequest.of(0, 10));
        ExperienceSearchResponse experiences =
            experienceSearchService.searchExperiences(request.q(), PageRequest.of(0, 10));

        return new UnifiedSearchResponse(products, farms, experiences);
    }
}
