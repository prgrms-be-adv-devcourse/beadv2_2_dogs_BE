package com.barofarm.support.review.presentation;

import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.review.application.dto.response.ReviewDetailInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "My-Reviews", description = "나의 리뷰 관련 API")
@RequestMapping("/api/v1/me/reviews")
public interface MyReviewSwaggerApi {

    @Operation(
        summary = "내 리뷰 목록 조회",
        description = "로그인한 사용자가 작성한 리뷰 목록을 페이지 단위로 조회한다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "내 리뷰 목록 조회 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "요청 값 검증 실패 (userId 헤더 누락 또는 형식 오류)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    ResponseDto<CustomPage<ReviewDetailInfo>> getMyReviews(
        @RequestHeader("X-Member-Id") UUID userId,
        Pageable pageable
    );
}
