package com.barofarm.ai.review.application.bestreview;

import com.barofarm.ai.review.application.summary.ReviewSummaryService;
import com.barofarm.ai.review.domain.bestreview.BestReviewListDocument;
import com.barofarm.ai.review.domain.review.Sentiment;
import com.barofarm.ai.review.infrastructure.bestreview.BestReviewListRepository;
import com.barofarm.ai.review.infrastructure.bestreview.ReviewCandidateQueryRepository;
import com.barofarm.ai.review.infrastructure.bestreview.ReviewCandidateQueryRepository.CandidateResult;
import com.barofarm.ai.review.infrastructure.bestreview.ReviewCandidateQueryRepository.CandidateReviews;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BestReviewListService {

    private static final int CANDIDATE_SIZE = 10;
    private static final double REPLACE_THRESHOLD = 0.5;

    private final ReviewCandidateQueryRepository queryRepository;
    private final BestReviewListRepository listRepository;
    private final BestReviewReplacementPolicy replacementPolicy;
    private final ReviewSummaryService summaryService;
    private final ElasticsearchOperations elasticsearchOperations;

    public void refreshProduct(String productId) {
        ensureBestReviewIndex();
        CandidateResult result;
        try {
            result = queryRepository.fetchCandidates(productId, CANDIDATE_SIZE);   // productId의 후보 생성
        } catch (IOException e) {
            log.warn("Failed to fetch candidates. productId={}", productId, e);
            return;
        }

        refreshBySentiment(
            productId,
            Sentiment.POSITIVE,
            result.positiveCount(),
            result.positive()
        );
        refreshBySentiment(
            productId,
            Sentiment.NEGATIVE,
            result.negativeCount(),
            result.negative()
        );
    }

    private void refreshBySentiment(String productId, Sentiment sentiment, long count, CandidateReviews reviews) {
        ensureBestReviewIndex();
        if (count < CANDIDATE_SIZE) {
            return;
        }

        List<String> newIds = reviews.reviewIds();
        String docId = BestReviewListDocument.buildId(productId, sentiment);
        Optional<BestReviewListDocument> existing = listRepository.findById(docId);
        List<String> existingIds = existing.map(BestReviewListDocument::getReviewIds).orElse(List.of());

        boolean shouldReplace = replacementPolicy.shouldReplace(existingIds, newIds, REPLACE_THRESHOLD);
        if (!shouldReplace) {
            return;
        }

        BestReviewListDocument updated =
            new BestReviewListDocument(productId, sentiment, newIds, Instant.now());
        listRepository.save(updated);
        summaryService.summarizeFromContents(productId, sentiment, reviews.contents());
    }

    private void ensureBestReviewIndex() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(BestReviewListDocument.class);
        if (!indexOps.exists()) {
            indexOps.createWithMapping();
        }
    }
}
