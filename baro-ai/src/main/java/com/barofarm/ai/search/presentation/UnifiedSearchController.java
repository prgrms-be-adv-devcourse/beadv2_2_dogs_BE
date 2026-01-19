package com.barofarm.ai.search.presentation;

import com.barofarm.ai.search.application.UnifiedSearchService;
import com.barofarm.ai.search.application.dto.UnifiedAutoCompleteResponse;
import com.barofarm.ai.search.application.dto.UnifiedSearchResponse;
import com.barofarm.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "통합 검색")
@RestController
@RequestMapping("${api.v1}/search")
@RequiredArgsConstructor
public class UnifiedSearchController {
    private final UnifiedSearchService unifiedSearchService;

    @Operation(summary = "통합 검색", description = "키워드로 상품, 체험을 통합 검색")
    @GetMapping
    public ResponseDto<UnifiedSearchResponse> search(
        @Parameter(description = "검색어", example = "토마토") @RequestParam String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        UnifiedSearchResponse response = unifiedSearchService.search(q, pageable);
        return ResponseDto.ok(response);
    }

    @Operation(summary = "통합 자동완성", description = "키워드로 상품, 체험 자동완성 반환")
    @GetMapping("/autocomplete")
    public ResponseDto<UnifiedAutoCompleteResponse> autocomplete(
        @Parameter(description = "자동완성 검색어", example = "토마") @RequestParam String q,
        @Parameter(description = "제품 자동완성 값 개수") @RequestParam(required = false, defaultValue = "5") int pSize,
        @Parameter(description = "체험 자동완성 값 개수") @RequestParam(required = false, defaultValue = "5") int eSize) {
        UnifiedAutoCompleteResponse response = unifiedSearchService.autocomplete(q, pSize, eSize);
        return ResponseDto.ok(response);
    }
}
