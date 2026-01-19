package com.barofarm.ai.search.application;

import com.barofarm.ai.search.application.dto.UnifiedAutoCompleteResponse;
import com.barofarm.ai.search.application.dto.UnifiedSearchResponse;
import com.barofarm.ai.search.application.dto.experience.ExperienceAutoCompleteResponse;
import com.barofarm.ai.search.application.dto.experience.ExperienceSearchResponse;
import com.barofarm.ai.search.application.dto.product.ProductAutoCompleteResponse;
import com.barofarm.ai.search.application.dto.product.ProductSearchResponse;
import com.barofarm.dto.CustomPage;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnifiedSearchService {
    private final ProductSearchService productSearchService;
    private final ExperienceSearchService experienceSearchService;

    // 통합 검색
    public UnifiedSearchResponse search(String q, Pageable pageable) {
        // 비동기 처리를 위해 CompletableFuture 사용 (두 개의 ES 쿼리 병렬 실행)
        CompletableFuture<CustomPage<ProductSearchResponse>> productsFuture =
            CompletableFuture.supplyAsync(() -> productSearchService.searchProducts(q, pageable));
        CompletableFuture<CustomPage<ExperienceSearchResponse>> experiencesFuture =
            CompletableFuture.supplyAsync(() -> experienceSearchService.searchExperiences(q, pageable));

        return new UnifiedSearchResponse(
            productsFuture.join(),
            experiencesFuture.join()
        );
    }

    // 통합 자동완성
    public UnifiedAutoCompleteResponse autocomplete(String q, int pSize, int eSize) {
        // 비동기 처리를 위해 CompletableFuture 사용 (두 개의 ES 쿼리 병렬 실행)
        CompletableFuture<List<ProductAutoCompleteResponse>> productsFuture =
            CompletableFuture.supplyAsync(() -> productSearchService.autocomplete(q, pSize));
        CompletableFuture<List<ExperienceAutoCompleteResponse>> experiencesFuture =
            CompletableFuture.supplyAsync(() -> experienceSearchService.autocomplete(q, eSize));

        return new UnifiedAutoCompleteResponse(
            productsFuture.join(),
            experiencesFuture.join()
        );
    }
}
