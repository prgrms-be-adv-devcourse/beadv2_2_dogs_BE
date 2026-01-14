package com.barofarm.ai.ranking.presentation;

import com.barofarm.ai.common.response.ResponseDto;
import com.barofarm.ai.ranking.application.RankingService;
import com.barofarm.ai.ranking.dto.RankingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품 랭킹 API 컨트롤러
 */
@Tag(name = "상품 랭킹", description = "로그 기반 상품 랭킹 API")
@RestController
@RequestMapping("${api.v1}/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(
        summary = "Top 3 상품 랭킹 조회",
        description = "현재 시간 기준 1시간 전 로그를 분석하여 Top 3 상품 랭킹을 반환합니다."
    )
    @GetMapping("products/top")
    public ResponseEntity<ResponseDto<RankingResponse>> getTopRankings() {
        RankingResponse response = rankingService.getTopRankings();
        return ResponseEntity.ok(ResponseDto.ok(response));
    }
}
