package com.barofarm.ai.search.infrastructure.elasticsearch;

import com.barofarm.ai.search.domain.ProductAutocompleteDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductAutocompleteRepository
    extends ElasticsearchRepository<ProductAutocompleteDocument, UUID> {

    // "토마"가 입력되면 productName이 "토마"로 시작하고 status가 ON_SALE 또는 DISCOUNTED인 문서만 반환
    // size는 Pageable로 전달 (query 내부에 size 넣으면 ES parsing_exception 발생)
    @Query("""
        {
          "bool": {
            "must": {
              "match_phrase_prefix": {
                "productName": {
                  "query": "?0",
                  "max_expansions": 10
                }
              }
            },
            "filter": {
              "terms": {
                "status": ["ON_SALE", "DISCOUNTED"]
              }
            }
          }
        }
        """)
    List<ProductAutocompleteDocument> findByPrefix(String prefix, Pageable pageable);
}
