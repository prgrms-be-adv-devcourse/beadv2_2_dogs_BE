package com.barofarm.support.search.unified.application;

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

  // private final FarmSearchService farmSearchService;
  // private final ExperienceSearchService experienceSearchService;

  public UnifiedSearchResponse search(UnifiedSearchRequest request) {
    ProductSearchResponse products =
        productSearchService.searchProducts(request.q(), PageRequest.of(0, 10));

    // farm, experience 검색 추가

    return new UnifiedSearchResponse(products);
  }
}
