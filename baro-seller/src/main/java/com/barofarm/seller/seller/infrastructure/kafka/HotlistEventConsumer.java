package com.barofarm.seller.seller.infrastructure.kafka;

import com.barofarm.seller.seller.domain.Seller;
import com.barofarm.seller.seller.domain.SellerRepository;
import com.barofarm.seller.seller.domain.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotlistEventConsumer {

    private final ObjectMapper objectMapper;
    private final SellerRepository sellerRepository;

    // [1] auth-service에서 발행하는 hotlist 이벤트를 소비하여 seller 상태를 동기화한다.
    @KafkaListener(topics = "${opa.kafka.topic:opa-hotlist-events}")
    @Transactional
    public void onMessage(String payload) {
        if (payload == null || payload.isBlank()) {
            log.warn("[SELLER] hotlist 이벤트가 비어 있습니다.");
            return;
        }

        HotlistEvent event = parse(payload);
        if (event == null) {
            return;
        }

        if (!"seller".equalsIgnoreCase(event.getSubjectType())) {
            return;
        }

        UUID sellerId = parseUuid(event.getSubjectId());
        if (sellerId == null) {
            log.warn("[SELLER] hotlist 이벤트 subjectId가 유효하지 않습니다: {}", event.getSubjectId());
            return;
        }

        Status status = toStatus(event.getStatus());
        if (status == null) {
            log.warn("[SELLER] hotlist 이벤트 status가 유효하지 않습니다: {}", event.getStatus());
            return;
        }

        Optional<Seller> sellerOptional = sellerRepository.findById(sellerId);
        if (sellerOptional.isEmpty()) {
            log.warn("[SELLER] seller가 존재하지 않아 상태 동기화를 건너뜁니다. userId={}", sellerId);
            return;
        }

        Seller seller = sellerOptional.get();
        seller.changeStatus(status);
        sellerRepository.save(seller);
        log.info("[SELLER] 상태 동기화 완료. userId={}, status={}", sellerId, status);
    }

    private HotlistEvent parse(String payload) {
        try {
            return objectMapper.readValue(payload, HotlistEvent.class);
        } catch (Exception ex) {
            log.warn("[SELLER] hotlist 이벤트 파싱 실패: {}", ex.getMessage());
            return null;
        }
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Status toStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Status.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
