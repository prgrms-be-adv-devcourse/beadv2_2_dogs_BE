package com.barofarm.support.search.farm.infrastructure.elasticsearch;

import com.barofarm.support.search.farm.domain.FarmAutocompleteDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface FarmAutocompleteRepository
    extends ElasticsearchRepository<FarmAutocompleteDocument, UUID> {

    // "토마"가 입력되면 farmName이 "토마"로 시작하는 문서만 반환
    @Query("""
        {
          "query": {
            "match_phrase_prefix": {
              "farmName": {
                "query": "?0"
              }
            }
          }
        }
        """)
    List<FarmAutocompleteDocument> findByPrefix(String prefix);
}
