package com.barofarm.ai.search.presentation;

import com.barofarm.ai.search.application.ExperienceSearchService;
import com.barofarm.ai.search.application.dto.experience.ExperienceAutoCompleteResponse;
import com.barofarm.ai.search.application.dto.experience.ExperienceSearchRequest;
import com.barofarm.ai.search.application.dto.experience.ExperienceSearchResponse;
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

@Tag(name = "체험 검색", description = "체험 검색 및 자동완성 API")
@RestController
@RequestMapping("${api.v1}/search/experience")
@RequiredArgsConstructor
public class ExperienceSearchController {

    private final ExperienceSearchService service;

    @Operation(summary = "체험 검색", description = "키워드로 체험을 검색 (체험만)")
    @GetMapping
    // 프론트는 Query Parameter로 보내고, 백엔드는 @ModelAttribute로 묶어서 받음.
    public CustomPage<ExperienceSearchResponse> searchExperiences(
        @Parameter(description = "검색 조건 DTO") @ModelAttribute ExperienceSearchRequest request,
        @Parameter(description = "페이지 정보") Pageable pageable) {
        return service.searchOnlyExperiences(request, pageable);
    }

    @Operation(summary = "체험 자동완성", description = "키워드로 체험명 자동완성 (체험만)")
    @GetMapping("/autocomplete")
    public List<ExperienceAutoCompleteResponse> autocomplete(
        @Parameter(description = "자동완성 키워드", example = "농") @RequestParam String query,
        @Parameter(description = "자동완성 값 개수") @RequestParam(required = false, defaultValue = "5") int size) {
        return service.autocomplete(query, size);
    }
}
