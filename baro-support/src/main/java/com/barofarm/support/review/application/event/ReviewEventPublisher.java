package com.barofarm.support.review.application.event;

import com.barofarm.support.review.event.ReviewEvent;
import com.barofarm.support.review.event.ReviewEvent.ReviewEventData;
import com.barofarm.support.review.event.ReviewEvent.ReviewEventType;
import com.barofarm.support.review.infrastructure.kafka.ReviewEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewEventPublisher {
    private final ReviewEventProducer reviewEventProducer;

    public void publishCreated(ReviewTransactionEvent event) {
        ReviewEvent reviewEvent = buildEvent(ReviewEventType.REVIEW_CREATED, event);
        reviewEventProducer.send(reviewEvent);
    }

    public void publishUpdated(ReviewTransactionEvent event) {
        ReviewEvent reviewEvent = buildEvent(ReviewEventType.REVIEW_UPDATED, event);
        reviewEventProducer.send(reviewEvent);

    }

    public void publishDeleted(ReviewTransactionEvent event) {
        ReviewEvent reviewEvent = buildEvent(ReviewEventType.REVIEW_DELETED, event);
        reviewEventProducer.send(reviewEvent);
    }

    private ReviewEvent buildEvent(ReviewEventType type, ReviewTransactionEvent event) {
        return ReviewEvent.builder()
            .type(type)
            .data(ReviewEventData.builder()
                .reviewId(event.getReviewId())
                .productId(event.getProductId())
                .rating(event.getRating())
                .content(event.getContent())
                .imageUrls(event.getImageUrls())
                .occurredAt(event.getOccurredAt())
                .build())
            .build();
    }
}
