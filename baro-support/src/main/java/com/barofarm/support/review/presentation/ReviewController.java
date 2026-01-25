package com.barofarm.support.review.presentation;

import com.barofarm.dto.ResponseDto;
import com.barofarm.support.review.application.ReviewService;
import com.barofarm.support.review.application.dto.response.ReviewDetailInfo;
import com.barofarm.support.review.presentation.dto.ReviewUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.v1}/reviews")
public class ReviewController implements ReviewSwaggerApi {

    private final ReviewService reviewService;

    @GetMapping("/{reviewId}")
    public ResponseDto<ReviewDetailInfo> getReviewDetail(
        @RequestHeader("X-User-Id") UUID userId,
        @PathVariable UUID reviewId
    ) {
        return ResponseDto.ok(reviewService.getReviewDetail(userId, reviewId));
    }

    @PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto<ReviewDetailInfo> updateReview(
        @RequestHeader("X-User-Id") UUID userId,
        @PathVariable UUID reviewId,
        @Valid @RequestPart("data") ReviewUpdateRequest request,
        @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ResponseDto.ok(reviewService.updateReview(request.toCommand(userId, reviewId), images));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseDto<Void> deleteReview(
        @RequestHeader("X-User-Id") UUID userId,
        @PathVariable UUID reviewId
    ) {
        reviewService.deleteReview(userId, reviewId);
        return ResponseDto.ok(null);
    }
}
