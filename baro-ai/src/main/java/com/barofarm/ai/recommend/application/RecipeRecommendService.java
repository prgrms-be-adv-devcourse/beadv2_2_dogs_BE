package com.barofarm.ai.recommend.application;

import com.barofarm.ai.recommend.application.config.RecommendProperties;
import com.barofarm.ai.recommend.application.dto.IngredientRecommendResponse;
import com.barofarm.ai.recommend.application.dto.ProductRecommendResponse;
import com.barofarm.ai.recommend.application.dto.RecipeRecommendResponse;
import com.barofarm.ai.recommend.domain.CandidateRecipePlan;
import com.barofarm.ai.recommend.domain.IngredientProcessingUtil;
import com.barofarm.ai.recommend.domain.OwnedIngredient;
import com.barofarm.ai.recommend.domain.RecipeCandidates;
import com.barofarm.ai.recommend.infrastructure.client.CartClient;
import com.barofarm.ai.recommend.infrastructure.client.dto.CartInfo;
import com.barofarm.ai.recommend.infrastructure.llm.RecipePromptService;
import com.barofarm.ai.search.application.ProductSearchService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeRecommendService {

    private static final int MISSING_PRODUCTS_LIMIT_PER_INGREDIENT = 2;

    private final CartClient cartClient;
    private final ProductSearchService productSearchService;
    private final RecipePromptService recipePromptService;
    private final RecommendProperties recommendProperties;

    public RecipeRecommendResponse recommendFromCartWithMissing(UUID userId) {
        try {
            CartInfo cart = cartClient.getCart(userId);
            return recommendFromCart(cart, userId != null ? userId.toString() : "null");
        } catch (Exception e) {
            // AI 서비스는 필수가 아니므로 장바구니 조회 실패 시 빈 응답 반환
            log.warn("장바구니 조회 실패로 레시피 추천을 건너뜁니다. userId: {}, error: {}", userId, e.getMessage());
            return new RecipeRecommendResponse("", List.of(), List.of(), List.of(), "");
        }
    }

    public RecipeRecommendResponse testRecommendFromCartWithMissing(CartInfo cart) {
        return recommendFromCart(cart, "test-user");
    }

    private RecipeRecommendResponse recommendFromCart(CartInfo cart, String userIdentifier) {
        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            log.warn("장바구니가 비어있습니다. userId: {}", userIdentifier);
            return new RecipeRecommendResponse("", List.of(), List.of(), List.of(), "");
        }

        final List<String> categoriesToExcludeByCode =
            recommendProperties.getExcludeRecipeCategoryCodes();

        List<RecipePromptService.CartItemInput> cartItems = cart.items().stream()
            .filter(Objects::nonNull)
            .filter(item -> {
                String categoryCode = safeTrim(item.productCategoryCode());

                boolean hasCodeRules = categoriesToExcludeByCode != null
                    && !categoriesToExcludeByCode.isEmpty();
                if (hasCodeRules && !categoryCode.isEmpty()) {
                    boolean excludedByCode = categoriesToExcludeByCode.contains(categoryCode);
                    if (excludedByCode) {
                        log.info("레시피 추천에서 '{}' 상품 제외 (카테고리 코드: {})",
                            item.productName(), categoryCode);
                        return false;
                    }
                }

                return true;
            })
            .map(i -> new RecipePromptService.CartItemInput(
                safeTrim(i.productName()),
                safeTrim(i.productCategoryName())
            ))
            .filter(i -> !i.productName().isBlank())
            .toList();

        if (cartItems.isEmpty()) {
            log.warn("유효한 장바구니 상품이 없습니다. userId: {}", userIdentifier);
            return new RecipeRecommendResponse("", List.of(), List.of(), List.of(), "");
        }

        Set<String> originalProductNames = cartItems.stream()
            .map(RecipePromptService.CartItemInput::productName)
            .collect(Collectors.toSet());

        List<OwnedIngredient> ownedIngredients = recipePromptService.extractOwnedIngredients(
            cartItems);

        List<String> ownedNames = ownedIngredients == null
            ? List.of()
            : ownedIngredients.stream()
                .filter(oi -> oi != null
                    && oi.name() != null
                    && !oi.name().isBlank()
                    && oi.sourceProductName() != null
                    && originalProductNames.contains(oi.sourceProductName()))
                .map(oi -> IngredientProcessingUtil.normalizeForCompare(oi.name()))
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if (ownedNames.isEmpty()) {
            log.warn("보유 재료 추출 실패/빈 결과. userId: {}", userIdentifier);
            return new RecipeRecommendResponse("", List.of(), List.of(), List.of(), "");
        }

        RecipeCandidates candidates = recipePromptService.generateRecipeCandidates(ownedNames);
        log.info("레시피 후보 평가 시작. 보유 재료: {}", ownedNames);

        Optional<CandidateRecipePlan> bestCandidateOpt = candidates.chooseBest(ownedNames);

        if (bestCandidateOpt.isEmpty()) {
            log.warn("레시피 후보 생성/선택 실패. userId: {}", userIdentifier);
            return new RecipeRecommendResponse("", ownedNames, List.of(), List.of(), "");
        }

        CandidateRecipePlan plan = bestCandidateOpt.get();
        log.info("최종 선택된 레시피: {} ({}/{}/{})", plan.getRecipeName(),
            plan.getCookingMethod(), plan.getDifficulty(), plan.getMealType());

        List<String> core = IngredientProcessingUtil.normalizeList(plan.getRecipeIngredientsCore());
        List<String> missingCore = IngredientProcessingUtil.subtractNormalized(core, ownedNames);

        List<IngredientRecommendResponse> missingRecommendations =
            missingCore.stream()
                .map(ingredient -> new IngredientRecommendResponse(
                    ingredient,
                    searchProductsByIngredientName(ingredient,
                        MISSING_PRODUCTS_LIMIT_PER_INGREDIENT)
                ))
                .filter(r -> r.products() != null && !r.products().isEmpty())
                .toList();

        return new RecipeRecommendResponse(
            plan.getRecipeName(),
            ownedNames,
            missingCore,
            missingRecommendations,
            Optional.ofNullable(plan.getInstructions()).orElse("")
        );
    }

    private List<ProductRecommendResponse> searchProductsByIngredientName(String ingredientName,
        int size) {
        if (ingredientName == null || ingredientName.isBlank()) {
            return List.of();
        }

        try {
            Pageable pageable = PageRequest.of(0, size);
            var searchResult = productSearchService.searchProducts(null, ingredientName, pageable);

            return searchResult.content().stream()
                .map(product -> new ProductRecommendResponse(
                    product.productId(),
                    product.productName(),
                    product.productCategoryName(),
                    product.price()
                ))
                .limit(size)
                .toList();

        } catch (Exception e) {
            log.error("ES 재료 상품 검색 실패. ingredientName={}, message={}", ingredientName,
                e.getMessage(), e);
            return List.of();
        }
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
