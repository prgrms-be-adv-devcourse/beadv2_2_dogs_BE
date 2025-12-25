package com.barofarm.support.search.farm.infrastructure.elasticsearch;

import com.barofarm.support.search.farm.domain.FarmDocument;
import java.util.UUID;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

// ElasticsearchRepository를 확장해 CRUD/검색 기본 기능 제공
public interface FarmSearchRepository extends ElasticsearchRepository<FarmDocument, UUID> {
}
