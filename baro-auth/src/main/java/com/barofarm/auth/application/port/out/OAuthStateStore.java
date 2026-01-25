package com.barofarm.auth.application.port.out;

import java.util.UUID;

/**
 * OAuth state를 발급/검증하는 포트.
 * CSRF 방어를 위해 상태값을 저장하고, 필요 시 사용자와 연결한다.
 */
public interface OAuthStateStore {

    String issueLoginState();

    boolean validateLoginState(String state);

    String issueLinkState(UUID userId);

    UUID consumeLinkState(String state);
}
