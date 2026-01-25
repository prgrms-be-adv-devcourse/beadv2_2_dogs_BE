package com.barofarm.ai.review.infrastructure.summary;

import com.barofarm.ai.review.domain.summary.ReviewSummaryDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ReviewSummaryRepository extends ElasticsearchRepository<ReviewSummaryDocument, String> {
}
