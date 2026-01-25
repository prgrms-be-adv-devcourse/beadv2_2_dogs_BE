package com.barofarm.buyer.product.application;

import com.barofarm.buyer.product.domain.ReviewSummary;
import com.barofarm.buyer.product.domain.ReviewSummaryRepository;
import com.barofarm.buyer.product.domain.ReviewSummarySentiment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewSummaryService {

    private final ReviewSummaryRepository reviewSummaryRepository;

    @Transactional
    public void upsert(UUID productId, ReviewSummarySentiment sentiment, List<String> summaryText,
                       LocalDateTime updatedAt) {
        List<String> normalizedSummary = summaryText == null ? List.of() : summaryText;
        ReviewSummary summary = reviewSummaryRepository
            .findByProductIdAndSentiment(productId, sentiment)
            .orElseGet(() -> ReviewSummary.create(productId, sentiment, normalizedSummary));

        summary.updateSummary(normalizedSummary, updatedAt);
        reviewSummaryRepository.save(summary);
    }
}
