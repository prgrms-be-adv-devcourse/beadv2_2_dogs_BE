package com.barofarm.support.review.presentation;

import com.barofarm.dto.ResponseDto;
import com.barofarm.support.review.application.dto.response.ReviewDetailInfo;
import com.barofarm.support.review.presentation.dto.ReviewUpdateRequest;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Reviews", description = "리뷰 단건 조회 / 수정 / 삭제 API")
public interface ReviewSwaggerApi {

    @Schema(name = "ReviewUpdateMultipartRequest")
    class ReviewUpdateMultipartRequest {

        @Schema(
            description = "리뷰 수정 정보(JSON). form-data part name: data",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        public ReviewUpdateRequest data;

        @ArraySchema(
            arraySchema = @Schema(description = "리뷰 이미지 파일 목록(선택). form-data part name: images"),
            schema = @Schema(type = "string", format = "binary")
        )
        public List<MultipartFile> images;
    }

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
        @RequestHeader("X-User-Id") UUID userId,
        @PathVariable UUID reviewId
    );

    // ===================== 리뷰 수정 =====================

    @Operation(
        summary = "리뷰 수정",
        description = "리뷰 작성자가 자신의 리뷰를 수정한다. " +
            "삭제되었거나 수정 불가능한 상태의 리뷰는 수정할 수 없다. " +
            "(multipart/form-data: data(JSON) + images(File, 선택))"
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
    @PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseDto<ReviewDetailInfo> updateReview(
        @RequestHeader("X-User-Id") UUID userId,
        @PathVariable UUID reviewId,
        @RequestBody(
            required = true,
            content = @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(implementation = ReviewUpdateMultipartRequest.class)
            )
        )
        @Valid @RequestPart("data") ReviewUpdateRequest request,
        @RequestPart(value = "images", required = false) List<MultipartFile> images
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
        @RequestHeader("X-User-Id") UUID userId,
        @PathVariable UUID reviewId
    );
}
