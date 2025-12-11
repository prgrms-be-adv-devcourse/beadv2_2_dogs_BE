package com.barofarm.support.search.product.presentation;

import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.search.product.application.ProductSearchService;
import com.barofarm.support.search.product.application.dto.ProductIndexRequest;
import com.barofarm.support.search.product.domain.ProductDocument;
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

/** 테스트용 상품 인덱싱 API - Kafka 연결 전까지 임시로 사용 - Kafka Consumer 구현 후 삭제 예정 */
@Tag(name = "상품 인덱싱 (테스트용)", description = "Kafka 연결 전까지 임시 사용")
@RestController
@RequestMapping("/api/v1/admin/search/products")
@RequiredArgsConstructor
public class ProductIndexingController {

  private final ProductSearchService productSearchService;

  @Operation(summary = "상품 인덱싱", description = "ES에 상품 문서를 저장합니다. Kafka 연결 후 삭제 예정.")
  @PostMapping
  public ResponseDto<ProductDocument> indexProduct(@RequestBody ProductIndexRequest request) {
    ProductDocument saved = productSearchService.indexProduct(request);
    return new ResponseDto<>(HttpStatus.CREATED, saved, null);
  }

  @Operation(summary = "상품 삭제", description = "ES에서 상품 문서를 삭제합니다. Kafka 연결 후 삭제 예정.")
  @DeleteMapping("/{productId}")
  public ResponseDto<Void> deleteProduct(
      @Parameter(description = "상품 ID", example = "1") @PathVariable UUID productId) {
    productSearchService.deleteProduct(productId);
    return new ResponseDto<>(HttpStatus.NO_CONTENT, null, null);
  }
}
