package com.barofarm.ai.log.application;

import com.barofarm.ai.log.domain.CartLogDocument;
import com.barofarm.ai.log.domain.OrderLogDocument;
import com.barofarm.ai.log.domain.SearchLogDocument;
import com.barofarm.ai.log.domain.PaymentLogDocument;
import com.barofarm.ai.log.infrastructure.elasticsearch.CartLogRepository;
import com.barofarm.ai.log.infrastructure.elasticsearch.OrderLogRepository;
import com.barofarm.ai.log.infrastructure.elasticsearch.SearchLogRepository;
import com.barofarm.ai.log.infrastructure.elasticsearch.PaymentLogRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogWriteService {

    private final CartLogRepository cartLogRepository;
    private final OrderLogRepository orderLogRepository;
    private final SearchLogRepository searchLogRepository;
    private final PaymentLogRepository paymentLogRepository;

    public void saveCartEventLog(UUID userId,
                                 UUID productId,
                                 String productName,
                                 String categoryName,
                                 String eventType,
                                 Integer quantity,
                                 Instant occurredAt) {
        CartLogDocument document = CartLogDocument.builder()
            .userId(userId)
            .productId(productId)
            .productName(productName)
            .categoryName(categoryName)
            .eventType(eventType)
            .quantity(quantity)
            .occurredAt(occurredAt)
            .build();

        CartLogDocument saved = cartLogRepository.save(document);
        log.info("Saved cart event log - ID: {}, User: {}, Product: {}, Category: {}",
            saved.getId(), userId, productName, categoryName);
    }

    public void saveOrderEventLog(UUID userId,
                                  UUID productId,
                                  String productName,
                                  String categoryName,
                                  String eventType,
                                  Integer quantity,
                                  Instant occurredAt) {
        OrderLogDocument document = OrderLogDocument.builder()
            .userId(userId)
            .productId(productId)
            .productName(productName)
            .categoryName(categoryName)
            .eventType(eventType)
            .quantity(quantity)
            .occurredAt(occurredAt)
            .build();

        OrderLogDocument saved = orderLogRepository.save(document);
        log.info("Saved order event log - ID: {}, User: {}, Product: {}, Category: {}",
            saved.getId(), userId, productName, categoryName);
    }

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
        log.info("Saved search log - ID: {}, User: {}, Query: '{}'",
            saved.getId(), userId, searchQuery);
    }

    public void savePaymentEventLog(UUID userId,
                                    UUID paymentId,
                                    UUID orderId,
                                    Long amount,
                                    String purpose,
                                    String eventType,
                                    Instant occurredAt) {
        PaymentLogDocument document = PaymentLogDocument.builder()
            .userId(userId)
            .paymentId(paymentId)
            .orderId(orderId)
            .amount(amount)
            .purpose(purpose)
            .eventType(eventType)
            .occurredAt(occurredAt)
            .build();

        PaymentLogDocument saved = paymentLogRepository.save(document);
        log.info("Saved payment event log - ID: {}, User: {}, Payment: {}, Order: {}, Purpose: {}, Amount: {}",
            saved.getId(), userId, paymentId, orderId, purpose, amount);
    }
}
