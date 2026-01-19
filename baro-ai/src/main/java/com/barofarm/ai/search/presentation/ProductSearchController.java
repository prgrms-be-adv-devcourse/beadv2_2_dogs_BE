package com.barofarm.ai.search.presentation;

import com.barofarm.ai.search.application.ProductSearchService;
import com.barofarm.ai.search.application.dto.product.ProductAutoCompleteResponse;
import com.barofarm.ai.search.application.dto.product.ProductSearchRequest;
import com.barofarm.ai.search.application.dto.product.ProductSearchResponse;
import com.barofarm.dto.CustomPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "상품 검색", description = "상품 검색 및 자동완성 API")
@RestController
@RequestMapping("${api.v1}/search/product")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService service;

    @Operation(summary = "상품 검색", description = "키워드로 상품을 검색 (상품만)")
    @GetMapping
    // 프론트는 Query Parameter로 보내고, 백엔드는 @ModelAttribute로 묶어서 받음.
    public CustomPage<ProductSearchResponse> searchProducts(
        @Parameter(description = "검색 조건 DTO") @ModelAttribute ProductSearchRequest request,
        @Parameter(description = "페이지 정보") Pageable pageable) {
        return service.searchOnlyProducts(request, pageable);
    }

    @Operation(summary = "상품 자동완성", description = "키워드로 상품명 자동완성 (상품만)")
    @GetMapping("/autocomplete")
    public List<ProductAutoCompleteResponse> autocomplete(
        @Parameter(description = "자동완성 키워드", example = "사") @RequestParam String query,
        @Parameter(description = "자동완성 값 개수") @RequestParam(required = false, defaultValue = "5") int size
    ) {
        return service.autocomplete(query, size);
    }
}
