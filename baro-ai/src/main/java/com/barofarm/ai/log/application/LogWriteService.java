package com.barofarm.ai.log.application;

import com.barofarm.ai.log.domain.CartLogDocument;
import com.barofarm.ai.log.domain.OrderLogDocument;
import com.barofarm.ai.log.domain.SearchLogDocument;
import com.barofarm.ai.log.infrastructure.elasticsearch.CartLogRepository;
import com.barofarm.ai.log.infrastructure.elasticsearch.OrderLogRepository;
import com.barofarm.ai.log.infrastructure.elasticsearch.SearchLogRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 개인화 추천을 위한 로그 쓰기 서비스
 * Kafka Consumer에서 호출되어 사용자 행동 로그를 Elasticsearch에 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogWriteService {

    private final CartLogRepository cartLogRepository;
    private final OrderLogRepository orderLogRepository;
    private final SearchLogRepository searchLogRepository;

    /**
     * 장바구니 이벤트 로그 저장
     */
    public void saveCartEventLog(UUID userId,
                                 UUID productId,
                                 String productName,
                                 String categoryCode,
                                 String eventType,
                                 Integer quantity,
                                 Instant occurredAt) {
        CartLogDocument document = CartLogDocument.builder()
            .userId(userId)
            .productId(productId)
            .productName(productName)
            .categoryCode(categoryCode)
            .eventType(eventType)
            .quantity(quantity)
            .occurredAt(occurredAt)
            .build();

        CartLogDocument saved = cartLogRepository.save(document);
        log.info("[LOG_WRITE] Saved cart event log - ID: {}, User: {}, Product: {}, CategoryCode: {}",
            saved.getId(), userId, productName, categoryCode);
    }

    /**
     * 주문 이벤트 로그 저장
     */
    public void saveOrderEventLog(UUID userId,
                                  UUID productId,
                                  String productName,
                                  String categoryCode,
                                  String eventType,
                                  Integer quantity,
                                  Instant occurredAt) {
        OrderLogDocument document = OrderLogDocument.builder()
            .userId(userId)
            .productId(productId)
            .productName(productName)
            .categoryCode(categoryCode)
            .eventType(eventType)
            .quantity(quantity)
            .occurredAt(occurredAt)
            .build();

        OrderLogDocument saved = orderLogRepository.save(document);
        log.info("[LOG_WRITE] Saved order event log - ID: {}, User: {}, Product: {}, CategoryCode: {}",
            saved.getId(), userId, productName, categoryCode);
    }

    /**
     * 검색 로그 저장
     */
    public void saveSearchLog(UUID userId,
                              String searchQuery,
                              String category,
                              Instant searchedAt) {
        SearchLogDocument document = SearchLogDocument.builder()
            .userId(userId)
            .searchQuery(searchQuery)
            .category(category)
            .searchedAt(searchedAt)
            .build();

        SearchLogDocument saved = searchLogRepository.save(document);
        log.info("[LOG_WRITE] Saved search log - ID: {}, User: {}, Query: '{}'",
            saved.getId(), userId, searchQuery);
    }
}
