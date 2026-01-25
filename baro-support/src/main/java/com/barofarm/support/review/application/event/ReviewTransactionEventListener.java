package com.barofarm.support.review.application.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReviewTransactionEventListener {

    private final ReviewEventPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewTransactionEvent(ReviewTransactionEvent event) {
        switch(event.getOperation()) {
            case CREATED -> publisher.publishCreated(event);
            case UPDATED -> publisher.publishUpdated(event);
            case DELETED -> publisher.publishDeleted(event);
            default -> {
            }
        }
    }
}
