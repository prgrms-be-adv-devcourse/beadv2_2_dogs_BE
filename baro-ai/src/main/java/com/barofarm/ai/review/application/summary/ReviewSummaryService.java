package com.barofarm.ai.review.application.summary;

import com.barofarm.ai.event.model.ReviewSummaryEvent;
import com.barofarm.ai.review.domain.review.Sentiment;
import com.barofarm.ai.review.domain.summary.ReviewSummaryDocument;
import com.barofarm.ai.review.infrastructure.kafka.ReviewSummaryEventProducer;
import com.barofarm.ai.review.infrastructure.summary.ReviewSummaryRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewSummaryService {

    private final ChatClient chatClient;
    private final ReviewSummaryRepository summaryRepository;
    private final ReviewSummaryEventProducer eventProducer;

    public void summarizeFromContents(String productId, Sentiment sentiment, List<String> contents) {
        if (contents == null || contents.isEmpty()) {
        return;
    }

        List<String> summaryLines = requestSummary(productId, sentiment, contents);
        if (summaryLines == null || summaryLines.isEmpty()) {
            return;
        }

        ReviewSummaryDocument document = new ReviewSummaryDocument(
            productId,
            sentiment,
            summaryLines,
            Instant.now()
        );
        summaryRepository.save(document);
        UUID productUuid = parseProductId(productId);
        if (productUuid != null) {
            eventProducer.send(new ReviewSummaryEvent(
                productUuid,
                sentiment.name(),
                summaryLines,
                document.getUpdatedAt()
            ));
        }
    }

    private List<String> requestSummary(String productId, Sentiment sentiment, List<String> contents) {
        String prompt = buildPrompt(productId, sentiment, contents);
        try {
            SummaryResponse response = chatClient.prompt()
                .user(u -> u.text(prompt))
                .call()
                .entity(SummaryResponse.class);
            if (response == null || response.summary() == null || response.summary().isEmpty()) {
                return List.of();
            }
            return response.summary()
                .stream()
                .map(String::valueOf)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        } catch (Exception e) {
            log.warn("요약에 실패했습니다. productId={} sentiment={}", productId, sentiment, e);
            return List.of();
        }
    }

    private String buildPrompt(String productId, Sentiment sentiment, List<String> contents) {
        String joined = contents.stream()
            .map(c -> "- " + c.replace("\n", " ").trim())
            .collect(Collectors.joining("\n"));

        return """
        너는 쇼핑몰 상품 리뷰를 요약하는 역할이다.

        아래 리뷰들은 특정 상품(ProductId)의 리뷰 중,
        "%s" 감정(POSITIVE 또는 NEGATIVE)에 해당하는 리뷰들이다.

        작업 목표:
        - 리뷰 요약 느낌이 나도록, 핵심만 간단히 정리해 주세요.
        - 여러 리뷰에서 반복적으로 나타나는 특징만 뽑아 주세요.
        - 말투는 "~요" 체로 부드럽게 작성해 주세요.

        요약 기준:
        - 최대 3줄까지만 작성해 주세요.
        - 의미 있는 내용이 부족하면 1~2줄만 작성해도 돼요.
        - 각 줄은 한 문장으로, 짧고 명확하게 작성해 주세요.
        - "리뷰에 따르면", "사용자들은" 같은 표현은 사용하지 마세요.
        - 감정(%s)에 맞는 내용만 포함해 주세요.

        ProductId: %s

        리뷰 목록:
        %s

        출력 형식(JSON):
        {{"summary":["요약 문장 1","요약 문장 2","요약 문장 3"]}}
        """.formatted(
            sentiment.name(),
            sentiment.name(),
            productId,
            joined
        );
    }

    private record SummaryResponse(List<String> summary) {}

    private java.util.UUID parseProductId(String productId) {
        try {
            return java.util.UUID.fromString(productId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid productId for summary event. productId={}", productId, e);
            return null;
        }
    }
}
