package com.barofarm.ai.review.domain.review;

import com.barofarm.ai.event.model.ReviewEvent;
import com.barofarm.ai.event.model.ReviewEvent.ReviewEventData;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Document(indexName = "review_event")
@NoArgsConstructor
public class ReviewDocument {

    @Id
    private String id; // reviewId.toString()

    @Field(type = FieldType.Keyword)
    private String productId;

    private Integer rating;

    private String content;

    private List<String> imageUrls;

    private Integer contentLength;

    private Integer imageCount;

    @Field(type = FieldType.Keyword)
    private Sentiment sentiment;

    private LocalDateTime occurredAt;

    private ReviewDocument(ReviewEventData data, Sentiment sentiment) {
        String content = data.content();
        List<String> imageUrls = data.imageUrls();

        this.id = data.reviewId().toString();
        this.productId = data.productId().toString();
        this.rating = data.rating();
        this.content = content;
        this.imageUrls = imageUrls;
        this.contentLength = (content == null) ? 0 : content.length();
        this.imageCount = (imageUrls == null) ? 0 : imageUrls.size();
        this.sentiment = sentiment;
        this.occurredAt = data.occurredAt();
    }

    public static ReviewDocument from(ReviewEvent event, Sentiment sentiment) {
        ReviewEventData data = event.data();

        return new ReviewDocument(data, sentiment);
    }
}
