package com.barofarm.ai.log.infrastructure.elasticsearch;

import com.barofarm.ai.log.domain.PaymentLogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PaymentLogRepository extends ElasticsearchRepository<PaymentLogDocument, String> {
}

