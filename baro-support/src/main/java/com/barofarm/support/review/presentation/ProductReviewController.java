package com.barofarm.support.review.presentation;

import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.review.application.ReviewService;
import com.barofarm.support.review.application.dto.request.ReviewCreateCommand;
import com.barofarm.support.review.application.dto.response.ReviewDetailInfo;
import com.barofarm.support.review.presentation.dto.ReviewCreateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.v1}/products/{productId}/reviews")
public class ProductReviewController implements ProductSwaggerApi{

    private final ReviewService reviewService;

    @PostMapping
    public ResponseDto<ReviewDetailInfo> createReview(
        @PathVariable UUID productId,
        @RequestHeader("X-User-Id") UUID userId,
        @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewCreateCommand command =
            request.toCommand(productId, userId);

        return ResponseDto.ok(reviewService.createReview(command));
    }

    @GetMapping
    public ResponseDto<CustomPage<ReviewDetailInfo>> getReviewsByProductId(
        @PathVariable UUID productId,
        Pageable pageable
    ) {
        return ResponseDto.ok(reviewService.getReviewByProductId(productId, pageable));
    }
}
