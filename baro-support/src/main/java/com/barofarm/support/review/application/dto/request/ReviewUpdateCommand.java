package com.barofarm.support.review.application.dto.request;

import com.barofarm.support.review.domain.ReviewStatus;

public record ReviewUpdateCommand(
    Integer rating,
    ReviewStatus reviewStatus,
    String content) {
}
