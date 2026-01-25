package com.barofarm.support.review.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Kafka로 발행되는 Review 이벤트
 * ai-service에서 소비하여 감정 분석에 활용
 */
@Getter
@Builder
public class ReviewEvent {

    private ReviewEventType type;
    private ReviewEventData data;

    public enum ReviewEventType {
        REVIEW_CREATED,
        REVIEW_UPDATED,
        REVIEW_DELETED
    }

    @Getter
    @Builder
    public static class ReviewEventData {
        private UUID reviewId;
        private UUID productId;
        private Integer rating;
        private String content;
        private List<String> imageUrls;
        private LocalDateTime occurredAt;
    }
}
