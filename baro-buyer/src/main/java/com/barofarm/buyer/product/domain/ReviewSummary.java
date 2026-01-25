package com.barofarm.buyer.product.domain;

import com.barofarm.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "product_review_summary",
    uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "sentiment"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReviewSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewSummarySentiment sentiment;

    @Column(name = "summary_text", nullable = false, columnDefinition = "TEXT")
    @Convert(converter = ReviewSummaryLinesConverter.class)
    private List<String> summaryText;

    private ReviewSummary(UUID productId, ReviewSummarySentiment sentiment, List<String> summaryText) {
        this.productId = productId;
        this.sentiment = sentiment;
        this.summaryText = summaryText;
    }

    public static ReviewSummary create(UUID productId, ReviewSummarySentiment sentiment, List<String> summaryText) {
        return new ReviewSummary(productId, sentiment, summaryText);
    }

    public void updateSummary(List<String> summaryText) {
        this.summaryText = summaryText;
        updateTimestamp();
    }

    public void updateSummary(List<String> summaryText, LocalDateTime updatedAt) {
        this.summaryText = summaryText;
        updateTimestamp(updatedAt);
    }
}
