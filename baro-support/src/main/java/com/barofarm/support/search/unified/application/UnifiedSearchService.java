package com.barofarm.support.search.unified.application;

import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.search.experience.application.ExperienceAutocompleteService;
import com.barofarm.support.search.experience.application.ExperienceSearchService;
import com.barofarm.support.search.experience.application.dto.ExperienceAutoItem;
import com.barofarm.support.search.experience.application.dto.ExperienceSearchItem;
import com.barofarm.support.search.farm.application.FarmAutocompleteService;
import com.barofarm.support.search.farm.application.FarmSearchService;
import com.barofarm.support.search.farm.application.dto.FarmAutoItem;
import com.barofarm.support.search.farm.application.dto.FarmSearchItem;
import com.barofarm.support.search.product.application.ProductAutocompleteService;
import com.barofarm.support.search.product.application.ProductSearchService;
import com.barofarm.support.search.product.application.dto.ProductAutoItem;
import com.barofarm.support.search.product.application.dto.ProductSearchItem;
import com.barofarm.support.search.unified.application.dto.UnifiedAutoCompleteResponse;
import com.barofarm.support.search.unified.application.dto.UnifiedSearchResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnifiedSearchService {
    private final ProductSearchService productSearchService;
    private final FarmSearchService farmSearchService;
    private final ExperienceSearchService experienceSearchService;

    private final ProductAutocompleteService productAutoService;
    private final FarmAutocompleteService farmAutoService;
    private final ExperienceAutocompleteService experienceAutoService;

    // 통합 검색
    public UnifiedSearchResponse search(String q, Pageable pageable) {
        // 각 서비스에서 CustomPage를 직접 반환받아서 조합
        CustomPage<ProductSearchItem> products = productSearchService.searchProducts(q, pageable);
        CustomPage<FarmSearchItem> farms = farmSearchService.searchFarms(q, pageable);
        CustomPage<ExperienceSearchItem> experiences = experienceSearchService.searchExperiences(q, pageable);

        return new UnifiedSearchResponse(products, farms, experiences);
    }

    // 통합 자동완성
    public UnifiedAutoCompleteResponse autocomplete(String q) {
        List<ProductAutoItem> autoProducts = productAutoService.autocomplete(q);
        List<FarmAutoItem> autoFarms = farmAutoService.autocomplete(q);
        List<ExperienceAutoItem> autoExperiences = experienceAutoService.autocomplete(q);

        return new UnifiedAutoCompleteResponse(autoProducts, autoFarms, autoExperiences); // 최대 15개 반환됨.
    }
}
