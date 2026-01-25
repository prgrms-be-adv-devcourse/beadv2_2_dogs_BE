package com.barofarm.ai.recommend.domain;

import static com.barofarm.ai.recommend.domain.IngredientProcessingUtil.normalizeList;

import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

// AI가 추천하는 레시피 후보 정보 도메인
@Data
@NoArgsConstructor
public class CandidateRecipePlan {

    private String cookingMethod;              // 요리 방법 (예: "볶음", "찌개", "구이")
    private String difficulty;                  // 난이도 ("EASY", "NORMAL", "CHALLENGE")
    private String mealType;                   // 식사 종류 (예: "점심", "저녁", "간식")
    private String recipeName;                 // 레시피 이름
    private List<String> recipeIngredientsCore;    // 레시피의 필수 재료 목록
    private List<String> recipeIngredientsExtra;   // 레시피의 추가 재료 목록 (선택사항)
    private String instructions;               // 조리법 설명
    private boolean staplesAssumed;            // 기본 양념(소금, 후추 등)은 이미 있다고 가정
    private List<String> usedOwnedIngredients;     // 사용자가 보유한 재료 중 이 레시피에서 사용되는 것들
    private List<String> missingIngredientsCore;   // 이 레시피를 만들기 위해 부족한 필수 재료들

    // 레시피 후보의 점수 계산
    public int calculateScore(Set<String> ownedNorm) {
        // 정규화 결과를 한 번만 계산하여 재사용 (중복 정규화 제거)
        List<String> normalizedCore = normalizeList(this.recipeIngredientsCore).stream()
            .map(IngredientProcessingUtil::normalizeForCompare)
            .filter(s -> !s.isBlank())
            .toList();

        List<String> normalizedExtra = normalizeList(this.recipeIngredientsExtra).stream()
            .map(IngredientProcessingUtil::normalizeForCompare)
            .filter(s -> !s.isBlank())
            .toList();

        long ownedCoreCount = normalizedCore.stream()
            .filter(ownedNorm::contains)
            .count();

        long ownedExtraCount = normalizedExtra.stream()
            .filter(ownedNorm::contains)
            .count();

        long missingCoreCount = normalizedCore.stream()
            .filter(s -> !ownedNorm.contains(s))
            .count();

        int missingBonus;
        if (missingCoreCount == 0) {
            missingBonus = -10; // 모든 재료 보유 시 판매 기회 없음 (감점)
        } else if (missingCoreCount <= 2) {
            missingBonus = 15;  // 1-2개 부족 시 판매 기회 높음 (가점)
        } else {
            missingBonus = -5;  // 3개 이상 부족 시 사용자가 포기할 수 있음 (감점)
        }

    // 점수: (보유 핵심 재료 × 10) + (보유 추가 재료 × 3) + (부족 재료 보너스)
    return (int) (ownedCoreCount * 10 + ownedExtraCount * 3 + missingBonus);
    }
}
