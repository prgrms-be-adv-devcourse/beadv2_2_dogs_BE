package com.barofarm.ai.recommend.presentation;

import com.barofarm.ai.recommend.application.PersonalizedRecommendService;
import com.barofarm.ai.recommend.application.RecipeRecommendService;
import com.barofarm.ai.recommend.application.SimilarProductRecommendService;
import com.barofarm.ai.recommend.application.dto.ProductRecommendResponse;
import com.barofarm.ai.recommend.application.dto.RecipeRecommendResponse;
import com.barofarm.ai.recommend.infrastructure.client.dto.CartInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "추천", description = "개인화 추천 및 레시피 추천 API")
@RestController
@RequestMapping("${api.v1}/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final PersonalizedRecommendService personalizedRecommendService;
    private final RecipeRecommendService recipeRecommendService;
    private final SimilarProductRecommendService similarProductRecommendService;

    @Operation(
        summary = "개인화 추천 상품 조회",
        description = "사용자의 행동 로그를 기반으로 생성된 프로필 벡터와 상품 벡터의 유사도를 계산하여 개인화된 상품을 추천"
    )
    @GetMapping("/personalized/{userId}")
    public List<ProductRecommendResponse> getPersonalizedRecommendations(
        @Parameter(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable UUID userId,
        @Parameter(description = "추천할 상품 개수", example = "5")
        @RequestParam(required = false, defaultValue = "5") int topK
    ) {
        return personalizedRecommendService.recommendProducts(userId, topK);
    }

    @Operation(
        summary = "사용자의 장바구니 기반 레시피 추천 (테스트용)",
        description = "CartInfo를 입력받아 레시피를 추천하고, 부족한 핵심 재료에 대한 상품 추천을 제공"
    )
    @PostMapping("/recipes/test")
    public RecipeRecommendResponse testRecommendFromCartWithMissing(
        @Parameter(description = "테스트할 장바구니 정보")
        @RequestBody CartInfo cart
    ) {
        return recipeRecommendService.testRecommendFromCartWithMissing(cart);
    }

    @Operation(
        summary = "사용자의 장바구니 기반 레시피 추천",
        description = "사용자의 실제 장바구니 상품을 기반으로 맞춤 레시피를 추천하고, 부족한 핵심 재료에 대한 상품 추천을 제공"
    )
    @GetMapping("/recipes/{userId}")
    public RecipeRecommendResponse recommendFromCartWithMissing(
        @Parameter(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable UUID userId
    ) {
        return recipeRecommendService.recommendFromCartWithMissing(userId);
    }

    @Operation(
        summary = "특정 상품과 유사한 상품 추천",
        description = "상품 상세 페이지 등에서 사용. 상품의 임베딩 벡터를 기반으로 유사도가 높은 다른 상품들을 추천"
    )
    @GetMapping("/similar/{productId}")
    public List<ProductRecommendResponse> getSimilarProducts(
        @Parameter(description = "기준 상품 ID", example = "00a1a1a1-a1a1-1a1a-a1a1-a1a1a1a1a1a1")
        @PathVariable UUID productId,
        @Parameter(description = "추천할 상품 개수", example = "3")
        @RequestParam(required = false, defaultValue = "3") int topK
    ) {
        return similarProductRecommendService.recommendSimilarProducts(productId, topK);
    }
}
