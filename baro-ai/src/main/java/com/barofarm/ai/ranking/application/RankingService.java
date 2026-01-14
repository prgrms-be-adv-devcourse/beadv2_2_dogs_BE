package com.barofarm.ai.ranking.application;

import com.barofarm.ai.ranking.domain.ProductRankingScore;
import com.barofarm.ai.ranking.dto.ProductLogData;
import com.barofarm.ai.ranking.dto.RankingResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

/**
 * 랭킹 계산 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final LogFileReaderService logFileReaderService;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    /**
     * Top 3 상품 랭킹 계산 및 반환
     * 하이브리드 접근: 1차 필터링(간단한 점수) -> 2차 정제(LLM) -> Top 3 추출
     */
    public RankingResponse getTopRankings() {
        // 1. 로그 파일 읽기
        LogFileReaderService.LogFileData logData = logFileReaderService.readPreviousHourLogs();

        // 2. productId 기준으로 데이터 집계
        Map<UUID, ProductRankingScore> scoreMap = aggregateProductData(
            logData.clickLogs(),
            logData.dwellTimeLogs()
        );

        if (scoreMap.isEmpty()) {
            log.warn("랭킹 데이터가 없습니다.");
            return new RankingResponse(
                List.of(),
                new RankingResponse.AnalyzedPeriod(logData.date(), logData.hour())
            );
        }

        log.info("1차 필터링 시작: {}개 상품", scoreMap.size());

        // 3. 1차 필터링: 간단한 점수로 Top 15 후보 선정
        List<ProductRankingScore> candidates = scoreMap.values().stream()
            .peek(score -> score.setScore(calculateSimpleScore(score))) // 간단한 점수 계산
            .sorted(Comparator.comparing(ProductRankingScore::getScore).reversed())
            .limit(15) // Top 15 후보 선정 (Top 3를 확실히 얻기 위해 여유를 둠)
            .collect(Collectors.toList());

        log.info("1차 필터링 완료: {}개 후보 선정", candidates.size());

        // 4. 2차 정제: 선정된 후보에 대해서만 LLM 호출
        calculateScoresWithLLM(candidates);

        // 5. 최종 Top 3 추출
        List<RankingResponse.ProductRanking> top3 = candidates.stream()
            .sorted(Comparator.comparing(ProductRankingScore::getScore).reversed())
            .limit(3)
            .map(score -> new RankingResponse.ProductRanking(
                null, // rank는 나중에 설정
                score.getProductId(),
                score.getProductName(),
                score.getScore(),
                score.getClickCount(),
                score.getAverageDwellTimeMs()
            ))
            .collect(Collectors.toList());

        // 6. 순위 설정
        for (int i = 0; i < top3.size(); i++) {
            top3.set(i, new RankingResponse.ProductRanking(
                i + 1,
                top3.get(i).productId(),
                top3.get(i).productName(),
                top3.get(i).score(),
                top3.get(i).clickCount(),
                top3.get(i).averageDwellTimeMs()
            ));
        }

        log.info("랭킹 계산 완료: Top {}개 반환", top3.size());

        return new RankingResponse(
            top3,
            new RankingResponse.AnalyzedPeriod(logData.date(), logData.hour())
        );
    }

    /**
     * 클릭 로그와 체류 시간 로그를 productId 기준으로 집계
     */
    private Map<UUID, ProductRankingScore> aggregateProductData(
        List<ProductLogData> clickLogs,
        List<ProductLogData> dwellTimeLogs
    ) {
        Map<UUID, ProductRankingScore> scoreMap = new HashMap<>();

        // 클릭 로그 집계
        for (ProductLogData clickLog : clickLogs) {
            UUID productId = clickLog.productId();
            scoreMap.computeIfAbsent(
                productId,
                id -> new ProductRankingScore(id, clickLog.productName())
            ).incrementClick();
        }

        // 체류 시간 로그 집계
        for (ProductLogData dwellTimeLog : dwellTimeLogs) {
            UUID productId = dwellTimeLog.productId();
            if (dwellTimeLog.dwellTimeMs() != null) {
                scoreMap.computeIfAbsent(
                    productId,
                    id -> new ProductRankingScore(id, dwellTimeLog.productName())
                ).addDwellTime(dwellTimeLog.dwellTimeMs());
            }
        }

        log.info("집계 완료: {}개 상품", scoreMap.size());
        return scoreMap;
    }

    /**
     * LLM을 통한 랭킹 점수 계산 (선정된 후보에 대해서만 호출)
     */
    private void calculateScoresWithLLM(List<ProductRankingScore> candidates) {
        log.info("2차 정제 시작: {}개 후보에 대해 LLM 호출", candidates.size());
        for (ProductRankingScore score : candidates) {
            try {
                String prompt = buildRankingPrompt(score);
                ChatResponse response = chatClient.call(new Prompt(prompt));
                String responseText = response.getResult().getOutput().getContent();

                Double calculatedScore = parseLLMResponse(responseText);
                score.setScore(calculatedScore);

                log.debug("상품 {} LLM 점수 계산 완료: {}", score.getProductId(), calculatedScore);
            } catch (Exception e) {
                log.error("LLM 점수 계산 실패 (상품: {}): {}", score.getProductId(), e.getMessage());
                // LLM 실패 시 간단한 점수 유지 (이미 1차 필터링에서 계산됨)
                // 점수는 그대로 유지 (calculateSimpleScore로 이미 계산됨)
            }
        }
        log.info("2차 정제 완료: {}개 후보 처리 완료", candidates.size());
    }

    /**
     * LLM 프롬프트 생성
     */
    private String buildRankingPrompt(ProductRankingScore score) {
        return String.format("""
            다음 상품의 클릭 수와 체류 시간 데이터를 분석하여 인기도 점수를 0~100 사이의 실수로 계산해주세요.
            
            상품 정보:
            - 상품 ID: %s
            - 상품명: %s
            - 클릭 횟수: %d
            - 평균 체류 시간: %d 밀리초 (%.2f 초)
            - 총 체류 시간: %d 밀리초
            
            점수 계산 기준:
            1. 클릭 횟수가 많을수록 높은 점수
            2. 체류 시간이 길수록 높은 점수 (사용자가 상품에 관심이 많다는 의미)
            3. 두 요소를 종합적으로 고려하여 점수 계산
            
            응답 형식은 반드시 다음 JSON 형식으로만 응답해주세요:
            {
              "score": 85.5,
              "reasoning": "높은 클릭 수와 긴 체류 시간을 고려하여 높은 점수 부여"
            }
            
            JSON만 응답하고 다른 설명은 포함하지 마세요.
            """,
            score.getProductId(),
            score.getProductName(),
            score.getClickCount(),
            score.getAverageDwellTimeMs(),
            score.getAverageDwellTimeMs() / 1000.0,
            score.getTotalDwellTimeMs()
        );
    }

    /**
     * LLM 응답 파싱
     */
    private Double parseLLMResponse(String responseText) {
        try {
            // JSON 추출 (마크다운 코드 블록 제거)
            String jsonText = responseText.trim();
            if (jsonText.startsWith("```json")) {
                jsonText = jsonText.substring(7);
            }
            if (jsonText.startsWith("```")) {
                jsonText = jsonText.substring(3);
            }
            if (jsonText.endsWith("```")) {
                jsonText = jsonText.substring(0, jsonText.length() - 3);
            }
            jsonText = jsonText.trim();

            JsonNode jsonNode = objectMapper.readTree(jsonText);
            JsonNode scoreNode = jsonNode.get("score");

            if (scoreNode != null && scoreNode.isNumber()) {
                double score = scoreNode.asDouble();
                // 0~100 범위로 제한
                return Math.max(0.0, Math.min(100.0, score));
            }
        } catch (Exception e) {
            log.warn("LLM 응답 파싱 실패: {}", responseText, e);
        }

        // 파싱 실패 시 기본값 반환
        return 50.0;
    }

    /**
     * 1차 필터링용 간단한 점수 계산 (클릭 수 + 체류 시간 기반)
     * LLM 호출 전에 후보를 선정하기 위한 빠른 점수 계산
     *
     * TODO: 향후 추가 예정 필드 반영 필요
     * - 누적 판매량 (totalSalesCount): 판매 실적 기반 점수 추가
     * - 좋아요 수 (likeCount): 사용자 선호도 기반 점수 추가
     */
    private Double calculateSimpleScore(ProductRankingScore score) {
        // 클릭 수 기반 점수 (0~50점)
        // 클릭 수가 많을수록 높은 점수 (최대 50점)
        // TODO: 누적판매량, 좋아요 추가 시 가중치 조정 필요 (현재 50점)
        double clickScore = Math.min(50.0, (score.getClickCount() / 10.0));

        // 체류 시간 기반 점수 (0~30점)
        // 평균 체류 시간이 길수록 높은 점수 (최대 30점)
        // 체류 시간이 없으면 0점
        double dwellTimeScore = 0.0;
        if (score.getAverageDwellTimeMs() != null && score.getAverageDwellTimeMs() > 0) {
            // 평균 체류 시간(초) * 0.6 (최대 30초 = 30점)
            dwellTimeScore = Math.min(30.0, (score.getAverageDwellTimeMs() / 1000.0) * 0.6);
        }

        // TODO: 누적 판매량 기반 점수 추가 (0~15점 예정)
        // double salesScore = 0.0;
        // if (score.getTotalSalesCount() != null && score.getTotalSalesCount() > 0) {
        //     // 판매량이 많을수록 높은 점수 (예: 판매량 / 100, 최대 15점)
        //     salesScore = Math.min(15.0, (score.getTotalSalesCount() / 100.0));
        // }

        // TODO: 좋아요 수 기반 점수 추가 (0~5점 예정)
        // double likeScore = 0.0;
        // if (score.getLikeCount() != null && score.getLikeCount() > 0) {
        //     // 좋아요가 많을수록 높은 점수 (예: 좋아요 수 / 20, 최대 5점)
        //     likeScore = Math.min(5.0, (score.getLikeCount() / 20.0));
        // }

        return clickScore + dwellTimeScore;
        // TODO: return clickScore + dwellTimeScore + salesScore + likeScore;
    }
}
