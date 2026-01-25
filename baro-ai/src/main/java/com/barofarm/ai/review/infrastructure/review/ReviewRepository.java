package com.barofarm.ai.review.infrastructure.review;

import com.barofarm.ai.review.domain.review.ReviewDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ReviewRepository extends ElasticsearchRepository<ReviewDocument, String> {
}
