package com.barofarm.ai.event.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReviewEvent(ReviewEventType type,
                          ReviewEventData data) {
    public enum ReviewEventType {
        REVIEW_CREATED,
        REVIEW_UPDATED,
        REVIEW_DELETED
    }

    public record ReviewEventData(UUID reviewId,
                                  UUID productId,
                                  Integer rating,
                                  String content,
                                  List<String> imageUrls,
                                  LocalDateTime occurredAt) {}
}
