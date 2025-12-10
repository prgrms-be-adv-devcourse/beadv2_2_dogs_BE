package com.barofarm.support.review.presentation;

import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.review.application.dto.response.ReviewDetailInfo;
import com.barofarm.support.review.presentation.dto.ReviewCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Review-Products", description = "제품 리뷰 관련 API")
@RequestMapping("/api/v1/products/{productId}/reviews")
public interface ProductSwaggerApi {

    @Operation(
        summary = "제품 리뷰 등록", description = "제품 리뷰를 등록한다."
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
    @PostMapping
    ResponseDto<ReviewDetailInfo> createReview(
        @PathVariable UUID productId,
        @RequestHeader("X-Member-Id") UUID userId,
        @Valid @RequestBody ReviewCreateRequest request
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
