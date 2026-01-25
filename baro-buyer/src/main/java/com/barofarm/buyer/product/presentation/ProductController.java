package com.barofarm.buyer.product.presentation;

import com.barofarm.buyer.product.application.ProductService;
import com.barofarm.buyer.product.application.dto.ProductDetailInfo;
import com.barofarm.buyer.product.domain.UserType;
import com.barofarm.buyer.product.presentation.dto.ProductCreateRequest;
import com.barofarm.buyer.product.presentation.dto.ProductUpdateRequest;
import com.barofarm.dto.CustomPage;
import com.barofarm.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.v1}/products")
public class ProductController {

  private final ProductService productService;

  @Operation(summary = "상품 상세 조회 API", description = "상품의 고유 ID를 이용하여 해당 상품의 상세 정보를 조회합니다.")
  @GetMapping("/{id}")
  public ResponseDto<ProductDetailInfo> getProductDetailInfo(@PathVariable UUID id) {
    return ResponseDto.ok(productService.getProductDetail(id));
  }

  @Operation(summary = "상품 모두 조회 API", description = "모든 상품의 정보를 조회합니다.")
  @GetMapping
  public ResponseDto<CustomPage<ProductDetailInfo>> getProducts(Pageable pageable) {
    return ResponseDto.ok(productService.getProducts(pageable));
  }

  @Operation(
      summary = "상품 생성 API (이미지 포함)",
      description = "multipart로 data(JSON) + images(파일)를 전송하여 상품을 생성합니다."
  )
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseDto<ProductDetailInfo> createProductWithImages(
      @RequestHeader("X-User-Id") UUID memberId,
      @RequestHeader("X-User-Role") UserType role,
      @Parameter(
          name = "data",
          required = true,
          content = @Content(schema = @Schema(implementation = ProductCreateRequest.class))
      )
      @Valid @RequestPart("data") ProductCreateRequest request,
      @Parameter(
          name = "images",
          content = @Content(
              array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
          )
      )
      @RequestPart(value = "images", required = false) java.util.List<MultipartFile> images) {
      return ResponseDto.ok(productService.createProduct(request.toCommand(memberId, role), images));
  }

  @Operation(
      summary = "상품 수정 API (이미지 포함)",
      description = "multipart로 data(JSON) + images(파일)를 전송하여 상품을 수정합니다."
  )
  @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseDto<ProductDetailInfo> updateProductWithImages(
      @RequestHeader("X-User-Id") UUID memberId,
      @RequestHeader("X-User-Role") UserType role,
      @PathVariable UUID id,
      @Parameter(
          name = "data",
          required = true,
          content = @Content(schema = @Schema(implementation = ProductUpdateRequest.class))
      )
      @Valid @RequestPart("data") ProductUpdateRequest request,
      @Parameter(
          name = "images",
          content = @Content(
              array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
          )
      )
      @RequestPart(value = "images", required = false) java.util.List<MultipartFile> images) {
    return ResponseDto.ok(productService.updateProduct(
        id,
        request.toCommand(memberId, role),
        images,
        request.imageUpdateMode()
    ));
  }

  @Operation(
      summary = "상품 삭제 API",
      description = "member의 role을 검증 한 후, 상품의 고유 ID를 이용하여 해당 상품을 삭제합니다.")
  @DeleteMapping("/{id}")
  public ResponseDto<Void> deleteProduct(
      @RequestHeader("X-User-Id") UUID memberId,
      @RequestHeader("X-User-Role") UserType role,
      @PathVariable UUID id) {
    productService.deleteProduct(id, memberId, role);
    return ResponseDto.ok(null);
  }
}
