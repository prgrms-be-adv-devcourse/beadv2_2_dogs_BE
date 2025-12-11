package com.barofarm.support.search.unified.presentation;

import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.search.unified.application.UnifiedSearchService;
import com.barofarm.support.search.unified.application.dto.UnifiedAutoCompleteResponse;
import com.barofarm.support.search.unified.application.dto.UnifiedSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "통합 검색")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class UnifiedSearchController {
    private final UnifiedSearchService unifiedSearchService;

    @Operation(summary = "통합 검색", description = "검색어로 상품, 농장, 체험을 통합 검색한다.")
    @GetMapping
    public ResponseDto<UnifiedSearchResponse> search(
        @Parameter(description = "검색어", example = "토마토") @RequestParam String q,
        @PageableDefault(size = 10) Pageable pageable) {
        UnifiedSearchResponse response = unifiedSearchService.search(q, pageable);
        return ResponseDto.ok(response);
    }

    @GetMapping("/autocomplete")
    public ResponseDto<UnifiedAutoCompleteResponse> autocomplete(
        @Parameter(description = "자동완성 검색어", example = "토마") @RequestParam String q) {
        UnifiedAutoCompleteResponse response = unifiedSearchService.autocomplete(q);
        return ResponseDto.ok(response);
    }
}
