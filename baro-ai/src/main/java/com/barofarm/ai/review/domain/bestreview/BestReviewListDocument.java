package com.barofarm.ai.review.domain.bestreview;

import com.barofarm.ai.review.domain.review.Sentiment;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Document(indexName = "product_best_review_index")
@NoArgsConstructor
public class BestReviewListDocument {

    @Id
    private String id; // productId + "_" + sentiment code

    @Field(type = FieldType.Keyword)
    private String productId;

    @Field(type = FieldType.Keyword)
    private Sentiment sentiment;

    @Field(type = FieldType.Keyword)
    private List<String> reviewIds;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant updatedAt;

    public BestReviewListDocument(String productId, Sentiment sentiment, List<String> reviewIds,
                                  Instant updatedAt) {
        this.id = buildId(productId, sentiment);
        this.productId = productId;
        this.sentiment = sentiment;
        this.reviewIds = reviewIds;
        this.updatedAt = updatedAt;
    }

    public static String buildId(String productId, Sentiment sentiment) {
        return productId + "_" + sentiment.code();
    }
}
