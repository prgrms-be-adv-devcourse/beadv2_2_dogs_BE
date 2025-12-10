package com.barofarm.support.search.experience.presentation;

import com.barofarm.support.search.experience.application.ExperienceSearchService;
import com.barofarm.support.search.experience.application.dto.ExperienceIndexRequest;
import com.barofarm.support.search.experience.domain.ExperienceDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 테스트용 체험 인덱싱 API - Kafka 연결 전까지 임시로 사용 - Kafka Consumer 구현 후 삭제 예정
 */
@Tag(name = "체험 인덱싱 (테스트용)", description = "Kafka 연결 전까지 임시 사용")
@RestController
@RequestMapping("/api/v1/admin/search/experiences")
@RequiredArgsConstructor
public class ExperienceIndexingController {

    private final ExperienceSearchService experienceSearchService;

    @Operation(summary = "체험 인덱싱", description = "ES에 체험 문서를 저장합니다. Kafka 연결 후 삭제 예정.")
    @PostMapping
    public ResponseEntity<ExperienceDocument> indexExperience(
        @RequestBody ExperienceIndexRequest request) {
        ExperienceDocument saved = experienceSearchService.indexExperience(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "체험 삭제", description = "ES에서 체험 문서를 삭제합니다. Kafka 연결 후 삭제 예정.")
    @DeleteMapping("/{experienceId}")
    public ResponseEntity<Void> deleteExperience(
        @Parameter(description = "체험 ID", example = "1") @PathVariable UUID experienceId) {
        experienceSearchService.deleteExperience(experienceId);
        return ResponseEntity.noContent().build();
    }
}
