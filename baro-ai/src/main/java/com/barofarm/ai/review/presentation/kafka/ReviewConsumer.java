package com.barofarm.ai.review.presentation.kafka;

import com.barofarm.ai.event.model.ReviewEvent;
import com.barofarm.ai.review.application.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewConsumer {

    private final ReviewService reviewService;

    @KafkaListener(
        topics = "review-events",
        groupId = "ai-service-review",
        containerFactory = "reviewEventListenerContainerFactory"
    )
    public void onMessage(ReviewEvent event) {
        switch(event.type()) {
            case REVIEW_CREATED -> reviewService.saveReview(event);
            case REVIEW_UPDATED -> reviewService.updateReview(event);
            case REVIEW_DELETED -> reviewService.deleteReview(event);
            default -> {}
        }
    }
}
