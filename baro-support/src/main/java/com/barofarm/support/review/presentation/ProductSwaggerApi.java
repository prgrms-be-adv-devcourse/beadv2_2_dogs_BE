package com.barofarm.support.review.presentation;

import com.barofarm.dto.CustomPage;
import com.barofarm.dto.ResponseDto;
import com.barofarm.support.review.application.dto.response.ReviewDetailInfo;
import com.barofarm.support.review.presentation.dto.ReviewCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Product Reviews", description = "제품 리뷰 관련 API")
public interface ProductSwaggerApi {

    @Schema(name = "ReviewCreateMultipartRequest")
    class ReviewCreateMultipartRequest {

        @Schema(
            description = "리뷰 생성 정보(JSON). form-data part name: data",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        public ReviewCreateRequest data;

        @ArraySchema(
            arraySchema = @Schema(description = "리뷰 이미지 파일 목록(선택). form-data part name: images"),
            schema = @Schema(type = "string", format = "binary")
        )
        public List<MultipartFile> images;
    }

    @Operation(
        summary = "제품 리뷰 등록", description = "제품 리뷰를 등록한다. (multipart/form-data: data(JSON) + images(File, 선택))"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "리뷰 등록 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = """
            요청 값 검증 실패
            - ORDER_NOT_COMPLETED
            - INVALID_ORDER_STATUS
            - INVALID_PRODUCT_STATUS
            - DUPLICATE_REVIEW
            - INVALID_RATING_VALUE
            """,
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "주문 구매자가 아님 (ORDER_NOT_OWNED_BY_USER)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = """
            리소스를 찾을 수 없음
            - ORDER_ITEM_NOT_FOUND
            - PRODUCT_NOT_FOUND
            """,
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequestBody(
        required = true,
        content = @Content(
            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            schema = @Schema(implementation = ReviewCreateMultipartRequest.class)
        )
    )
    ResponseDto<ReviewDetailInfo> createReview(
        @PathVariable UUID productId,
        @RequestHeader("X-User-Id") UUID userId,
        @Valid @RequestPart("data") ReviewCreateRequest request,
        @RequestPart(value = "images", required = false) List<MultipartFile> images
    );


    @Operation(
        summary = "상품 리뷰 목록 조회",
        description = "특정 상품에 대해 공개된 리뷰 목록을 페이지 단위로 조회한다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "리뷰 목록 조회 성공",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    ResponseDto<CustomPage<ReviewDetailInfo>> getReviewsByProductId(
        @PathVariable UUID productId,
        Pageable pageable
    );
}
