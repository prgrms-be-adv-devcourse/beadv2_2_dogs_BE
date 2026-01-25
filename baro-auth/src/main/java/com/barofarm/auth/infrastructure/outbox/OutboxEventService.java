package com.barofarm.auth.infrastructure.outbox;

import com.barofarm.auth.domain.outbox.OutboxEvent;
import com.barofarm.auth.domain.user.User;
import com.barofarm.auth.infrastructure.jpa.OutboxEventJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventJpaRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void enqueueUserWithdrawnEvent(User user, String reason) {
        // [0] 탈퇴 이벤트는 userId 중심으로 발행하고 PII는 포함하지 않는다.
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getId().toString());
        payload.put("reason", reason);
        payload.put("withdrawnAt", Instant.now().toString());

        String payloadJson = toJson(payload);
        OutboxEvent event = OutboxEvent.create(
            "user",
            user.getId().toString(),
            "USER_WITHDRAWN",
            payloadJson
        );

        // [1] 동일 트랜잭션 안에서 저장해 발행/저장 불일치 문제를 줄인다.
        outboxEventRepository.save(event);
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            // [2] 페이로드 직렬화 실패는 즉시 예외 처리해 트랜잭션을 롤백한다.
            throw new IllegalStateException("Failed to serialize outbox payload", ex);
        }
    }
}
