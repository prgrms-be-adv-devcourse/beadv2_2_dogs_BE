package com.barofarm.ai.season.infrastructure.knowledge;

import com.barofarm.ai.season.application.dto.SeasonalityDetectionResponse;
import com.barofarm.ai.season.infrastructure.embedding.SeasonalityKnowledgeDocument;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 제철 지식 데이터 저장 서비스
 *
 * LLM으로 생성된 데이터를 다음 위치에 저장:
 * 1. VectorStore
 * 2. CSV 파일
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "seasonality.rag.enabled", havingValue = "true", matchIfMissing = false)
public class SeasonalityKnowledgeStoreService {

    private final VectorStore vectorStore;
    private final String csvFilePath;

    public SeasonalityKnowledgeStoreService(
            @Qualifier("seasonalityVectorStore") VectorStore vectorStore,
            @Value("${seasonality.csv.path:/mnt/s3/dataset/season/seasonality-data.csv}") String csvFilePath) {
        this.vectorStore = vectorStore;
        this.csvFilePath = csvFilePath;
    }

    /**
     * LLM으로 생성된 제철 정보 저장
     *
     * @param detectedProductName LLM이 판단한 전체 상품명
     * @param category 카테고리
     * @param response LLM 응답
     */
    @Async
    public void storeLLMGeneratedKnowledge(
            String detectedProductName,
            String category,
            SeasonalityDetectionResponse response) {

        try {
            // 중복 체크: VectorStore에서 동일한 productName과 category가 있는지 확인
            if (isDuplicate(detectedProductName, category)) {
                log.debug("이미 존재하는 데이터: {} ({})", detectedProductName, category);
                return;
            }

            String seasonalityType = response.seasonalityType().name();
            String seasonalityValue = response.seasonalityValue();
            double confidence = response.confidence();

            // LLM이 판단한 농장체험 가능 여부 사용
            String farmExperienceNote =
                response.farmExperienceNote() != null && !response.farmExperienceNote().isEmpty()
                    ? response.farmExperienceNote()
                    : "농장체험 가능";  // LLM 응답이 없으면 기본값

            // 1. VectorStore에 저장
            saveToVectorStore(detectedProductName, category, seasonalityType,
                            seasonalityValue, confidence);

            // 2. CSV 파일에 추가 (농장체험 가능 여부 포함)
            appendToCsvFile(detectedProductName, category, seasonalityType,
                          seasonalityValue, response.reasoning(), farmExperienceNote);

            log.info("LLM 생성 데이터 저장 완료: {} ({})", detectedProductName, category);

        } catch (Exception e) {
            log.error("LLM 생성 데이터 저장 실패: {} ({})", detectedProductName, category, e);
            // 저장 실패는 치명적이지 않으므로 예외를 다시 던지지 않음
        }
    }

    /**
     * VectorStore에서 중복 체크
     *
     * @param productName 상품명
     * @param category 카테고리
     * @return 중복 여부
     */
    private boolean isDuplicate(String productName, String category) {
        try {
            // VectorStore에서 동일한 productName과 category로 검색
            String query = String.format("%s %s", productName, category);
            SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(10)  // 상위 10개 결과 확인
                .build();

            List<Document> documents = vectorStore.similaritySearch(searchRequest);

            // 검색 결과에서 정확히 일치하는 productName과 category가 있는지 확인
            return documents.stream()
                .anyMatch(doc -> {
                    var metadata = doc.getMetadata();
                    String docProductName = (String) metadata.getOrDefault("productName", "");
                    String docCategory = (String) metadata.getOrDefault("category", "");
                    return productName.equals(docProductName) && category.equals(docCategory);
                });
        } catch (Exception e) {
            log.warn("중복 체크 중 오류 발생: {} ({})", productName, category, e);
            // 오류 발생 시 중복이 아닌 것으로 간주하여 저장 진행
            return false;
        }
    }

    /**
     * VectorStore에 저장
     */
    private void saveToVectorStore(
            String productName,
            String category,
            String seasonalityType,
            String seasonalityValue,
            double confidence) {

        try {
            // content는 reasoning을 사용하거나 기본 텍스트 생성
            String content = String.format("%s의 제철은 %s입니다.", productName, seasonalityValue);

            Document document = SeasonalityKnowledgeDocument.createDocument(
                productName,
                category,
                content,
                seasonalityType,
                seasonalityValue,
                "LLM_GENERATED"
            );

            vectorStore.add(List.of(document));
            log.debug("VectorStore 저장 완료: {} ({})", productName, category);

        } catch (Exception e) {
            log.error("VectorStore 저장 실패: {} ({})", productName, category, e);
        }
    }

    /**
     * CSV 파일에 데이터 추가
     */
    private void appendToCsvFile(
            String productName,
            String category,
            String seasonalityType,
            String seasonalityValue,
            String reasoning,
            String farmExperienceNote) {

        try {
            Path path = Paths.get(csvFilePath);

            // 파일이 없으면 헤더 작성
            if (!Files.exists(path)) {
                try (FileWriter writer = new FileWriter(csvFilePath, false)) {
                    writer.write("productName,category,content,seasonalityType,seasonalityValue,sourceType,note\n");
                }
            }

            // CSV 라인 생성
            String content = reasoning != null && !reasoning.isEmpty()
                ? reasoning
                : String.format("%s의 제철은 %s입니다.", productName, seasonalityValue);

            // CSV 특수문자 처리: 쉼표를 다른 문자로 대체하여 일관성 유지
            // CSV 파일의 기존 형식과 일관성을 유지하기 위해 쉼표를 다른 문자로 대체
            content = normalizeCsvField(content);
            productName = normalizeCsvField(productName);

            String csvLine = String.format("%s,%s,%s,%s,%s,LLM_GENERATED,%s\n",
                productName,
                category,
                content,
                seasonalityType,
                seasonalityValue,
                farmExperienceNote
            );

            // 파일 끝에 추가 (append mode)
            try (FileWriter writer = new FileWriter(csvFilePath, true)) {
                writer.write(csvLine);
            }

            log.debug("CSV 파일 추가 완료: {} ({})", productName, category);

        } catch (IOException e) {
            log.error("CSV 파일 추가 실패: {} ({})", productName, category, e);
        }
    }

    /**
     * CSV 필드 정규화 (쉼표를 다른 문자로 대체하여 일관성 유지)
     *
     * CSV 파일의 기존 형식과 일관성을 유지하기 위해 쉼표를 다른 문자로 대체
     * 쉼표를 전각 쉼표(，) 또는 줄바꿈을 공백으로 대체
     */
    private String normalizeCsvField(String field) {
        if (field == null) {
            return "";
        }

        // 쉼표를 전각 쉼표로 대체 (CSV 필드 구분자와 충돌 방지)
        // 줄바꿈과 캐리지 리턴을 공백으로 대체
        return field.replace(",", "，")
                    .replace("\n", " ")
                    .replace("\r", " ")
                    .trim();
    }

}
