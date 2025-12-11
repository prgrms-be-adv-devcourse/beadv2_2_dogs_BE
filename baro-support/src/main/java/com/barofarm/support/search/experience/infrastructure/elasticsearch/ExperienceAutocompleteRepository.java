package com.barofarm.support.search.experience.infrastructure.elasticsearch;

import com.barofarm.support.search.experience.domain.ExperienceAutocompleteDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ExperienceAutocompleteRepository
    extends ElasticsearchRepository<ExperienceAutocompleteDocument, UUID> {

    // "토마"가 입력되면 experienceName이 "토마"로 시작하는 문서만 반환
    @Query("""
        {
          "query": {
            "match_phrase_prefix": {
              "experienceName": {
                "query": "?0"
              }
            }
          }
        }
        """)
    List<ExperienceAutocompleteDocument> findByPrefix(String prefix);
}
