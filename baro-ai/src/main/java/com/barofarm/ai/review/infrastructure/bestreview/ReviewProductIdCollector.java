package com.barofarm.ai.review.infrastructure.bestreview;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.CompositeAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.CompositeAggregationSource;
import co.elastic.clients.elasticsearch._types.aggregations.CompositeBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewProductIdCollector {

    private static final String INDEX_NAME = "review_event";
    private static final String AGG_NAME = "product_ids";
    private static final String AGG_FIELD = "productId";

    private final ElasticsearchClient client;

    public ProductIdPage fetchPage(int size, Map<String, FieldValue> afterKey) throws IOException {
        SearchResponse<Void> response = client.search(s -> s
            .index(INDEX_NAME)
            .size(0)   // 검색 결과 문서(hit)을 한 건도 가져오지 않음(productId 목록만 필요하기 때문)
            .aggregations(AGG_NAME, a -> a // product_ids 라는 이름으로 집계를 하나 생성
                .composite(c -> { // 집계 결과를 페이지네이션 할 수 있는 집계 방식
                    c.size(size);
                    c.sources(List.of(// composite가 어떤 기준으로 그룹핑할 건지 정의하는 부분
                        Map.of(AGG_FIELD, CompositeAggregationSource.of(src -> src
                            .terms(t -> t.field(AGG_FIELD))
                        ))
                    ));
                    if (afterKey != null && !afterKey.isEmpty()) {
                        c.after(afterKey);
                    }
                    return c;
                })
            ), Void.class);

        CompositeAggregate composite = response.aggregations().get(AGG_NAME).composite();
        List<String> productIds = new ArrayList<>();
        for (CompositeBucket bucket : composite.buckets().array()) {
            FieldValue value = bucket.key().get(AGG_FIELD);
            if (value != null && value.isString()) {
                productIds.add(value.stringValue());
            }
        }

        return new ProductIdPage(productIds, composite.afterKey());
    }

    public record ProductIdPage(List<String> productIds, Map<String, FieldValue> afterKey) {
    }
}
