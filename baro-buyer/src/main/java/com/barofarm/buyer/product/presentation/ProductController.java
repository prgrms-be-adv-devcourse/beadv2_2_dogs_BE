package com.barofarm.buyer.product.presentation;

import com.barofarm.buyer.common.response.ResponseDto;
import com.barofarm.buyer.product.application.ProductService;
import com.barofarm.buyer.product.application.dto.ProductDetailInfo;
import com.barofarm.buyer.product.presentation.dto.ProductCreateRequest;
import com.barofarm.buyer.product.presentation.dto.ProductUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

  private final ProductService productService;

  @Operation(summary = "상품 상세 조회 API", description = "상품의 고유 ID를 이용하여 해당 상품의 상세 정보를 조회합니다.")
  @GetMapping("/{id}")
  public ResponseDto<ProductDetailInfo> getProductDetailInfo(@PathVariable UUID id) {
    return ResponseDto.ok(productService.getProductDetail(id));
  }

  @Operation(summary = "상품 생성 API", description = "member의 role을 검증 한 후, 상품을 생성합니다.")
  @PostMapping
  public ResponseDto<ProductDetailInfo> createProduct(
      @RequestHeader("X-User-Id") UUID memberId,
      @RequestHeader("X-User-Role") String role,
      @Valid @RequestBody ProductCreateRequest request) {
    return ResponseDto.ok(productService.createProduct(request.toCommand(memberId, role)));
  }

  @Operation(
      summary = "상품 수정 API",
      description = "member의 role을 검증 한 후, 상품의 고유 ID를 이용하여 해당 상품을 수정합니다.")
  @PatchMapping("/{id}")
  public ResponseDto<ProductDetailInfo> updateProduct(
      @RequestHeader("X-User-Id") UUID memberId,
      @RequestHeader("X-User-Role") String role,
      @PathVariable UUID id,
      @Valid @RequestBody ProductUpdateRequest request) {
    return ResponseDto.ok(productService.updateProduct(id, request.toCommand(memberId, role)));
  }

  @Operation(
      summary = "상품 삭제 API",
      description = "member의 role을 검증 한 후, 상품의 고유 ID를 이용하여 해당 상품을 삭제합니다.")
  @DeleteMapping("/{id}")
  public ResponseDto<Void> deleteProduct(
      @RequestHeader("X-User-Id") UUID memberId,
      @RequestHeader("X-User-Role") String role,
      @PathVariable UUID id) {
    productService.deleteProduct(id, memberId, role);
    return ResponseDto.ok(null);
  }
}
