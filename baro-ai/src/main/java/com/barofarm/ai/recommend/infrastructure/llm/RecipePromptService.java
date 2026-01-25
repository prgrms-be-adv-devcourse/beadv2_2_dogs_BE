package com.barofarm.ai.recommend.infrastructure.llm;

import com.barofarm.ai.recommend.domain.CandidateRecipePlan;
import com.barofarm.ai.recommend.domain.OwnedIngredient;
import com.barofarm.ai.recommend.domain.RecipeCandidates;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipePromptService {

    private final ChatClient chatClient;
    private final ProductNameNormalizer productNameNormalizer;

    public record CartItemInput(String productName, String categoryName) {

    }

    private record NormalizedCartItem(String originalProductName, String normalizedProductName,
        String categoryName) {

    }

    /**
     * [LLM #1] 장바구니 상품에서 보유 재료 추출
     * 정규화된 상품명들을 LLM에게 전달하여 실제 요리 재료를 추출
     *
     * @param cartItems 장바구니 상품 목록 (이미 정규화됨)
     * @return 추출된 보유 재료 목록
     */
    public List<OwnedIngredient> extractOwnedIngredients(List<CartItemInput> cartItems) {
        // LLM의 JSON 응답 구조를 임시로 받기 위한 private record
        record LlmOwnedIngredientsResponse(List<OwnedIngredient> ownedIngredients) {

        }

        // 1. 각 상품명을 미리 정규화하여 더 명확한 입력 제공 (배치 처리)
        List<String> productNames = cartItems.stream()
            .map(RecipePromptService.CartItemInput::productName)
            .toList();

        Map<String, String> normalizedMap = productNameNormalizer.normalizeBatchForRecipeIngredient(
            productNames);

        List<NormalizedCartItem> normalizedItems = cartItems.stream()
            .map(item -> {
                String normalizedName = normalizedMap.getOrDefault(item.productName(), "");
                return new NormalizedCartItem(
                    item.productName(),  // 원본 유지
                    normalizedName,      // 정규화된 이름
                    item.categoryName()
                );
            })
            .filter(item -> !item.normalizedProductName().isEmpty())
            .toList();

        String cartItemsText = normalizedItems.stream()
            .map(i -> "- original: \"" + i.originalProductName() + "\", normalized: \""
                + i.normalizedProductName() + "\", category: \"" + i.categoryName() + "\"")
            .collect(Collectors.joining("\n"));

        String templateString = """
            당신은 농산물 이커머스 장바구니를 분석해 실제 요리에 쓰일 수 있는 '보유 재료'만 추출합니다.

            <입력(cartItems)>
            {cartItems}

            <중요 정책>
            - normalized 필드를 우선적으로 참고하되, original과 비교하여 검증하세요.

            <핵심 규칙(엄격)>
            - 반드시 normalized 필드의 재료명을 그대로 사용하세요. 다른 형태로 변경하지 마세요.
            - normalized 필드의 정제된 재료명을 우선적으로 사용하세요.
            - 반드시 입력 cartItems의 original 또는 normalized에서 직접 근거를 찾을 수 있는 재료만 추출하세요.
            - 입력에 없는 재료(예: 양파, 마늘 등)를 상상으로 추가하면 안 됩니다.
            - 각 재료명은 한 단어(공백 없음)만 허용합니다.
            - 중복은 제거하세요.
            - 모호하면 제외하세요(추측 금지).

            <출력(JSON only)>
            {{
              "ownedIngredients": [
                {{
                  "name": "한단어재료명",
                  "sourceProductName": "입력에 존재하는 original 원문 그대로",
                  "sourceCategoryName": "입력에 존재하는 category 원문 그대로"
                }}
              ]
            }}
            다른 텍스트는 절대 출력하지 마세요.
            """;

        try {
            LlmOwnedIngredientsResponse response = chatClient.prompt()
                .user(p -> p.text(templateString).param("cartItems", cartItemsText))
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
            return response != null && response.ownedIngredients() != null
                ? response.ownedIngredients() : List.of();
        } catch (Exception e) {
            log.error("보유 재료 추출 실패: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // -------------------------
    // LLM #2: ownedIngredients -> candidates (Korean/Japanese/Western)
    // -------------------------
    /**
     * [LLM #2] 보유 재료로 레시피 후보 생성
     * 보유 재료를 바탕으로 한식/일식/양식 각 1개씩 총 3개의 레시피 생성
     *
     * @param ownedIngredients 보유 재료 목록 (예: ["계란", "애호박", "두부"])
     * @return 3개의 레시피 후보
     */
    public RecipeCandidates generateRecipeCandidates(List<String> ownedIngredients) {
        try {
            String templateString = buildRecipeCandidateTemplate(ownedIngredients);
            RecipeCandidates result = callRecipeCandidateLLM(templateString);

            if (result != null && result.candidates() != null) {
                return processRecipeCandidates(result, ownedIngredients);
            }

            return result != null ? result : new RecipeCandidates(List.of());
        } catch (Exception e) {
            log.error("레시피 후보 생성 실패: {}", e.getMessage(), e);
            return new RecipeCandidates(List.of());
        }
    }

    /**
     * 레시피 후보 생성을 위한 템플릿 문자열 생성
     */
    private String buildRecipeCandidateTemplate(List<String> ownedIngredients) {
        String ownedText = ownedIngredients.stream()
            .map(s -> "- " + s)
            .collect(Collectors.joining("\n"));

        return """
            당신은 '집밥 레시피 추천 AI'입니다.
            아래 '보유 재료'를 활용하여 만들 수 있는 실제 요리 레시피 3가지를 추천합니다.

            <보유 재료(ownedIngredients)>
            {ownedIngredients}

            <정책>
            - 기본 양념(물, 소금, 후추, 식용유, 설탕, 식초, 간장 등)은 사용자가 이미 보유하고 있다고 가정합니다.
            - 따라서 기본 양념은 'recipeIngredientsCore'나 'recipeIngredientsExtra' 목록에 포함시키지 마세요.

            <재료 조합 가이드>
            - '보유 재료'들이 서로 잘 어울리지 않는 경우, 모든 재료를 한 번에 사용하려고 하지 마세요.
            - 대신, '보유 재료'의 일부만 사용하여 현실적이고 맛있는 요리를 만드세요.
            - 예시: '보유 재료'가 ["사과", "계란"]일 때, "사과 계란 찌개" 같은 이상한 요리 대신 "애플파이"(사과 사용)나 "계란찜"(계란 사용)을 제안하는 것이 훨씬 좋습니다.
            - 핵심은 '억지 조합'이 아닌 '현실적인 요리'를 추천하는 것입니다.

            <후보 생성 규칙>
            - 판매 기회를 고려하여, 완벽한 재료가 아닌 '적절히 부족한' 레시피를 우선 생성하세요.
            - 'recipeIngredientsCore' 재료의 30-50% 정도는 '보유 재료'에 없는 새로운 재료로 구성하여, 사용자에게 추가 구매를 자연스럽게 유도하세요.
            - 반드시 3개의 독립적인 레시피 후보를 생성해야 합니다.
            - 각 레시피는 한식, 양식, 중식, 일식 등 다양한 스타일을 가져야 합니다.
            - cookingMethod/difficulty/mealType 조합은 후보마다 달라야 합니다.
            - 각 후보 레시피는 '보유 재료' 중 최소 1개는 반드시 활용해야 합니다.
            - 'recipeIngredientsCore'에는 해당 요리의 정체성을 나타내는 가장 중요한 재료들을 3~5개 선정하세요.
            - 'recipeIngredientsExtra'에는 있으면 요리의 풍미를 더하지만 필수적이지는 않은 재료들을 1~3개 선정하세요.
            - 억지스러운 재료 조합을 피하고, 실제로 사람들이 즐겨 만드는 일반적인 요리를 제안해야 합니다.
            - 재료명은 일반적인 한 단어 명사(예: "돼지고기", "김치", "양파")로 통일하세요. '보유 재료'에 있는 이름에 얽매일 필요 없습니다.

            <현실성 검증>
            - 제안하는 레시피(recipeName)가 실제로 존재하는 요리인지 반드시 확인하세요.
            - 재료 조합이 현실적인지 확인하세요. (예: '삼겹살'과 '양파'는 볶음 요리에 잘 어울리지만, '삼겹살'과 '사과'로 찌개를 만드는 것은 비현실적입니다.)

            <출력 필드 정의>
            - cookingMethod: 조리 방식 ("ONE_POT", "STIR_FRY", "NO_COOK", "OVEN_BAKE")
            - difficulty: 난이도 ("EASY", "NORMAL", "CHALLENGE")
            - mealType: 식사 유형 ("MAIN", "SIDE", "SOUP")
            - recipeName: 요리 이름 (예: "돼지고기 김치찌개")
            - recipeIngredientsCore: 요리의 정체성을 결정하는 핵심 재료 (3~5개).
            - recipeIngredientsExtra: 있으면 더 좋은 추가 재료 (1~3개).
            - instructions: 3~6 단계의 간단한 조리법.
            - staplesAssumed: 기본 양념을 가정했는지 여부 (항상 true로 설정).

            <출력(JSON only)>
            {{
              "candidates": [
                {{
                  "cookingMethod": "ONE_POT",
                  "difficulty": "EASY",
                  "mealType": "SOUP",
                  "recipeName": "돼지고기 김치찌개",
                  "recipeIngredientsCore": ["돼지고기", "김치", "두부", "대파"],
                  "recipeIngredientsExtra": ["양파", "청양고추"],
                  "instructions": "1) ...\n2) ...",
                  "staplesAssumed": true
                }}
              ]
            }}
            다른 텍스트는 절대 출력하지 마세요.
            """.replace("{ownedIngredients}", ownedText);
    }

    /**
     * LLM을 호출하여 레시피 후보를 생성
     */
    private RecipeCandidates callRecipeCandidateLLM(String templateString) {
        return chatClient.prompt()
            .user(p -> p.text(templateString))
            .call()
            .entity(new ParameterizedTypeReference<>() {
            });
    }

    /**
     * LLM 응답을 처리하고 서버 검증을 적용
     */
    private RecipeCandidates processRecipeCandidates(RecipeCandidates result, List<String> ownedIngredients) {
        // LLM 응답을 서버 계산으로 보정
        List<CandidateRecipePlan> enhancedCandidates = result.candidates().stream()
            .map(candidate -> enhanceWithServerValidation(candidate, ownedIngredients))
            .toList();

        RecipeCandidates enhancedResult = new RecipeCandidates(enhancedCandidates);

        log.info("레시피 후보 생성 완료. 후보 개수: {}", enhancedResult.candidates().size());
        enhancedResult.candidates().forEach(candidate ->
            log.info("  - 후보: {} ({}/{}/{}), core: {}, extra: {}, used: {}, missing: {}",
                candidate.getRecipeName(),
                candidate.getCookingMethod(),
                candidate.getDifficulty(),
                candidate.getMealType(),
                candidate.getRecipeIngredientsCore(),
                candidate.getRecipeIngredientsExtra(),
                candidate.getUsedOwnedIngredients(),
                candidate.getMissingIngredientsCore())
        );

        return enhancedResult;
    }

    /**
     * LLM 응답을 서버 계산으로 보정하여 CSP 검증 필드들을 정확하게 채움
     */
    private CandidateRecipePlan enhanceWithServerValidation(
        CandidateRecipePlan candidate,
        List<String> ownedIngredients
    ) {
        // core + extra에 포함된 재료 중 ownedIngredients에 있는 것들
        List<String> allIngredients = Stream.concat(
            candidate.getRecipeIngredientsCore().stream(),
            candidate.getRecipeIngredientsExtra().stream()
        ).toList();

        List<String> accurateUsed = allIngredients.stream()
            .filter(ownedIngredients::contains)
            .distinct()
            .toList();

        // core 재료 중 ownedIngredients에 없는 것들
        List<String> accurateMissing = candidate.getRecipeIngredientsCore().stream()
            .filter(ing -> !ownedIngredients.contains(ing))
            .distinct()
            .toList();

        // 검증된 candidate 반환
        CandidateRecipePlan verifiedCandidate = new CandidateRecipePlan();
        verifiedCandidate.setCookingMethod(candidate.getCookingMethod());
        verifiedCandidate.setDifficulty(candidate.getDifficulty());
        verifiedCandidate.setMealType(candidate.getMealType());
        verifiedCandidate.setRecipeName(candidate.getRecipeName());
        verifiedCandidate.setRecipeIngredientsCore(candidate.getRecipeIngredientsCore());
        verifiedCandidate.setRecipeIngredientsExtra(candidate.getRecipeIngredientsExtra());
        verifiedCandidate.setInstructions(candidate.getInstructions());
        verifiedCandidate.setStaplesAssumed(candidate.isStaplesAssumed());
        verifiedCandidate.setUsedOwnedIngredients(accurateUsed);
        verifiedCandidate.setMissingIngredientsCore(accurateMissing);
        return verifiedCandidate;
    }
}
