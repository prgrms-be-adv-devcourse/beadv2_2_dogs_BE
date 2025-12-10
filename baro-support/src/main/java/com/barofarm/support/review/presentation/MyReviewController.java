package com.barofarm.support.review.presentation;

import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.review.application.ReviewService;
import com.barofarm.support.review.application.dto.response.ReviewDetailInfo;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MyReviewController implements MyReviewSwaggerApi {
    private final ReviewService reviewService;

    @GetMapping
    public ResponseDto<CustomPage<ReviewDetailInfo>> getMyReviews(
        @RequestHeader("X-Member-Id") UUID userId,
        Pageable pageable
    ) {
        return ResponseDto.ok(reviewService.getReviewByBuyerId(userId, pageable));
    }
}
