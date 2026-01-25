package com.barofarm.ai.review.infrastructure.bestreview;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.FilterAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.TopHitsAggregate;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.barofarm.ai.review.domain.review.Sentiment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ReviewCandidateQueryRepository {

    private static final String INDEX_NAME = "review_event";
    private static final String AGG_POSITIVE = "positive";
    private static final String AGG_NEGATIVE = "negative";
    private static final String AGG_TOP = "top";

    private final ElasticsearchClient client;

    public CandidateResult fetchCandidates(String productId, int size) throws IOException {
        SearchResponse<Void> response = client.search(s -> s
            .index(INDEX_NAME)
            .size(0)
            .query(q -> q.term(t -> t.field("productId").value(productId)))
            .aggregations(AGG_POSITIVE, a -> a
                .filter(f -> f.term(t -> t.field("sentiment").value(Sentiment.POSITIVE.name())))
                .aggregations(AGG_TOP, a2 -> a2.topHits(th -> th
                    .size(size)
                    .sort(s1 -> s1.field(f -> f.field("imageCount").order(SortOrder.Desc)))
                    .sort(s1 -> s1.field(f -> f.field("contentLength").order(SortOrder.Desc)))
                    .sort(s1 -> s1.field(f -> f.field("occurredAt").order(SortOrder.Desc)))
                    .source(src -> src.filter(f -> f.includes(List.of("content"))))
                ))
            )
            .aggregations(AGG_NEGATIVE, a -> a
                .filter(f -> f.term(t -> t.field("sentiment").value(Sentiment.NEGATIVE.name())))
                .aggregations(AGG_TOP, a2 -> a2.topHits(th -> th
                    .size(size)
                    .sort(s1 -> s1.field(f -> f.field("imageCount").order(SortOrder.Desc)))
                    .sort(s1 -> s1.field(f -> f.field("contentLength").order(SortOrder.Desc)))
                    .sort(s1 -> s1.field(f -> f.field("occurredAt").order(SortOrder.Desc)))
                    .source(src -> src.filter(f -> f.includes(List.of("content"))))
                ))
            ), Void.class);

        FilterAggregate posAgg = requireFilter(response.aggregations().get(AGG_POSITIVE));
        FilterAggregate negAgg = requireFilter(response.aggregations().get(AGG_NEGATIVE));

        CandidateReviews positive = extractReviews(posAgg);
        CandidateReviews negative = extractReviews(negAgg);

        return new CandidateResult(
            posAgg.docCount(),
            negAgg.docCount(),
            positive,
            negative
        );
    }

    private FilterAggregate requireFilter(Aggregate aggregate) {
        if (aggregate == null || aggregate.filter() == null) {
            return FilterAggregate.of(f -> f.docCount(0));
        }
        return aggregate.filter();
    }

    private CandidateReviews extractReviews(FilterAggregate filterAgg) {
        Aggregate topAgg = filterAgg.aggregations().get(AGG_TOP);
        if (topAgg == null || topAgg.topHits() == null) {
            return new CandidateReviews(List.of(), List.of());
        }

        TopHitsAggregate topHits = topAgg.topHits();
        List<String> ids = new ArrayList<>();
        List<String> contents = new ArrayList<>();
        for (Hit<?> hit : topHits.hits().hits()) {
            if (hit.id() != null) {
                ids.add(hit.id());
            }
            String content = extractContent(hit);
            if (StringUtils.hasText(content)) {
                contents.add(content);
            }
        }
        return new CandidateReviews(ids, contents);
    }

    private String extractContent(Hit<?> hit) {
        Object source = hit.source();
        if (source instanceof Map<?, ?> map) {
            Object content = map.get("content");
            return (content instanceof String) ? (String) content : null;
        }
        if (source instanceof JsonData json) {
            Map<?, ?> map = json.to(Map.class);
            Object content = map.get("content");
            return (content instanceof String) ? (String) content : null;
        }
        return null;
    }

    public record CandidateResult(
        long positiveCount,
        long negativeCount,
        CandidateReviews positive,
        CandidateReviews negative
    ) {
    }

    public record CandidateReviews(
        List<String> reviewIds,
        List<String> contents
    ) {
    }
}
