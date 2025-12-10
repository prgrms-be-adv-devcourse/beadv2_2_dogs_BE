package com.barofarm.support.review.presentation;

import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.review.application.dto.response.ReviewDetailInfo;
import com.barofarm.support.review.presentation.dto.ReviewUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Reviews", description = "리뷰 단건 조회 / 수정 / 삭제 API")
@RequestMapping("/api/v1/reviews")
public interface ReviewSwaggerApi {

    // ===================== 리뷰 상세 조회 =====================

    @Operation(
        summary = "리뷰 상세 조회",
        description = "리뷰 ID를 이용해 리뷰 상세 정보를 조회한다. " +
            "리뷰 소유자이거나 공개(PUBLIC) 상태인 경우에만 조회 가능하다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "리뷰 조회 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "리뷰 조회 권한 없음 (REVIEW_NOT_READABLE)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "리뷰를 찾을 수 없음 (REVIEW_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/{reviewId}")
    ResponseDto<ReviewDetailInfo> getReviewDetail(
        @RequestHeader("X-Member-Id") UUID userId,
        @PathVariable UUID reviewId
    );

    // ===================== 리뷰 수정 =====================

    @Operation(
        summary = "리뷰 수정",
        description = "리뷰 작성자가 자신의 리뷰를 수정한다. " +
            "삭제되었거나 수정 불가능한 상태의 리뷰는 수정할 수 없다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "리뷰 수정 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = """
                요청 값 검증 실패
                - INVALID_RATING_VALUE
                """,
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "리뷰 수정 권한 없음 (REVIEW_FORBIDDEN)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "리뷰를 찾을 수 없음 (REVIEW_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409",
            description = "리뷰 수정 불가 상태 (REVIEW_NOT_UPDATABLE)",
            content = @Content(mediaType = "application/json")
        )
    })
    @PutMapping("/{reviewId}")
    ResponseDto<ReviewDetailInfo> updateReview(
        @RequestHeader("X-Member-Id") UUID userId,
        @PathVariable UUID reviewId,
        @Valid @RequestBody ReviewUpdateRequest request
    );

    // ===================== 리뷰 삭제 =====================

    @Operation(
        summary = "리뷰 삭제",
        description = "리뷰 작성자가 자신의 리뷰를 삭제한다. (소프트 삭제)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "리뷰 삭제 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "리뷰 삭제 권한 없음 (REVIEW_FORBIDDEN)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "리뷰를 찾을 수 없음 (REVIEW_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 삭제된 리뷰 (REVIEW_ALREADY_DELETED)",
            content = @Content(mediaType = "application/json")
        )
    })
    @DeleteMapping("/{reviewId}")
    ResponseDto<Void> deleteReview(
        @RequestHeader("X-Member-Id") UUID userId,
        @PathVariable UUID reviewId
    );
}
