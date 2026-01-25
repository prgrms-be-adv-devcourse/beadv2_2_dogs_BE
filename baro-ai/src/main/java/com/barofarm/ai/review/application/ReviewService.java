package com.barofarm.ai.review.application;

import com.barofarm.ai.event.model.ReviewEvent;
import com.barofarm.ai.review.application.sentiment.SentimentClassifier;
import com.barofarm.ai.review.domain.review.ReviewDocument;
import com.barofarm.ai.review.domain.review.Sentiment;
import com.barofarm.ai.review.infrastructure.review.ReviewRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final SentimentClassifier sentimentClassifier;

    public ReviewDocument saveReview(ReviewEvent reviewEvent) {
        Sentiment sentiment = sentimentClassifier.classify(
            reviewEvent.data().rating(),
            reviewEvent.data().content()
        );
        ReviewDocument document = ReviewDocument.from(reviewEvent, sentiment);
        return reviewRepository.save(document);
    }

    public ReviewDocument updateReview(ReviewEvent reviewEvent) {
        Sentiment sentiment = sentimentClassifier.classify(
            reviewEvent.data().rating(),
            reviewEvent.data().content()
        );
        ReviewDocument document = ReviewDocument.from(reviewEvent, sentiment);
        return reviewRepository.save(document);
    }

    public ReviewDocument deleteReview(ReviewEvent reviewEvent) {
        String id = reviewEvent.data().reviewId().toString();
        Optional<ReviewDocument> existing = reviewRepository.findById(id);
        reviewRepository.deleteById(id);
        return existing.orElse(null);
    }
}
