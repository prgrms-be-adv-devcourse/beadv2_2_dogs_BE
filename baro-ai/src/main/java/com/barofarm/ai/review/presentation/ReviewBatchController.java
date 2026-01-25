package com.barofarm.ai.review.presentation;

import com.barofarm.ai.review.application.bestreview.BestReviewBatchJob;
import com.barofarm.ai.review.application.bestreview.BestReviewListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "리뷰 배치", description = "베스트 리뷰/요약 수동 실행 API")
@RestController
@RequestMapping("${api.v1}/reviews/batch")
@RequiredArgsConstructor
public class ReviewBatchController {

    private final BestReviewListService bestReviewListService;
    private final BestReviewBatchJob bestReviewBatchJob;

    @Operation(summary = "상품 단건 베스트 리뷰/요약 갱신")
    @PostMapping("/refresh")
    public Map<String, String> refreshProduct(@RequestParam UUID productId) {
        bestReviewListService.refreshProduct(productId.toString());
        return Map.of("status", "ok", "productId", productId.toString());
    }

    @Operation(summary = "전체 상품 베스트 리뷰/요약 배치 실행")
    @PostMapping("/refresh-all")
    public Map<String, String> refreshAll() {
        bestReviewBatchJob.run();
        return Map.of("status", "ok");
    }
}
