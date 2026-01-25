package com.barofarm.auth.infrastructure.oauth;

import com.barofarm.auth.application.port.out.OAuthStateStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryOAuthStateStore implements OAuthStateStore {

    private static final Duration STATE_TTL = Duration.ofMinutes(10);

    private final Clock clock;
    private final Map<String, Instant> loginStates = new ConcurrentHashMap<>();
    private final Map<String, LinkState> linkStates = new ConcurrentHashMap<>();

    public InMemoryOAuthStateStore(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String issueLoginState() {
        // 로그인 콜백용 state는 단발성으로 발급한다.
        String state = UUID.randomUUID().toString();
        loginStates.put(state, expiresAt());
        return state;
    }

    @Override
    public boolean validateLoginState(String state) {
        // 검증 후 바로 폐기하여 재사용을 막는다.
        Instant expiry = loginStates.remove(state);
        return expiry != null && expiry.isAfter(Instant.now(clock));
    }

    @Override
    public String issueLinkState(UUID userId) {
        // 계정 연결 state는 사용자와 1:1로 매핑한다.
        String state = UUID.randomUUID().toString();
        linkStates.put(state, new LinkState(userId, expiresAt()));
        return state;
    }

    @Override
    public UUID consumeLinkState(String state) {
        // 링크 state는 사용 즉시 제거해 CSRF 및 재사용을 방지한다.
        LinkState stored = linkStates.remove(state);
        if (stored == null || stored.expiresAt().isBefore(Instant.now(clock))) {
            return null;
        }
        return stored.userId();
    }

    private Instant expiresAt() {
        return Instant.now(clock).plus(STATE_TTL);
    }

    private record LinkState(UUID userId, Instant expiresAt) {
    }
}
