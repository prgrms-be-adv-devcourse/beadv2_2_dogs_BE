package com.barofarm.ai.review.infrastructure.bestreview;

import com.barofarm.ai.review.domain.bestreview.BestReviewListDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BestReviewListRepository
    extends ElasticsearchRepository<BestReviewListDocument, String> {
}
