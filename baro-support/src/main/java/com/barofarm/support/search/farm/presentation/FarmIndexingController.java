package com.barofarm.support.search.farm.presentation;

import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.search.farm.application.FarmSearchService;
import com.barofarm.support.search.farm.application.dto.FarmIndexRequest;
import com.barofarm.support.search.farm.domain.FarmDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 테스트용 농장 인덱싱 API - Kafka 연결 전까지 임시로 사용 - Kafka Consumer 구현 후 삭제 예정 */
@Tag(name = "농장 인덱싱 (테스트용)", description = "Kafka 연결 전까지 임시 사용")
@RestController
@RequestMapping("/api/v1/admin/search/farms")
@RequiredArgsConstructor
public class FarmIndexingController {

  private final FarmSearchService farmSearchService;

  @Operation(summary = "농장 인덱싱", description = "ES에 농장 문서를 저장합니다. Kafka 연결 후 삭제 예정.")
  @PostMapping
  public ResponseDto<FarmDocument> indexFarm(@RequestBody FarmIndexRequest request) {
    FarmDocument saved = farmSearchService.indexFarm(request);
    return new ResponseDto<>(HttpStatus.CREATED, saved, null);
  }

  @Operation(summary = "농장 삭제", description = "ES에서 농장 문서를 삭제합니다. Kafka 연결 후 삭제 예정.")
  @DeleteMapping("/{farmId}")
  public ResponseDto<Void> deleteFarm(
      @Parameter(description = "농장 ID", example = "1") @PathVariable UUID farmId) {
    farmSearchService.deleteFarm(farmId);
    return new ResponseDto<>(HttpStatus.NO_CONTENT, null, null);
  }
}
