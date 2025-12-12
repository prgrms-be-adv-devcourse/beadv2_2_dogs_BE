package com.barofarm.support.search.product.infrastructure.elasticsearch;

import com.barofarm.support.search.product.domain.ProductAutocompleteDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductAutocompleteRepository
    extends ElasticsearchRepository<ProductAutocompleteDocument, UUID> {

    // "토마"가 입력되면 productNam이 "토마"로 시작하는 문서만 찾아서 반환
    @Query("""
        {
                   "match_phrase_prefix": {
                     "productName": {
                       "query": "?0"
                     }
                   }
                 }
        """)
    List<ProductAutocompleteDocument> findByPrefix(String prefix);
}
