package com.barofarm.auth.application.port.out;

import com.barofarm.auth.domain.oauth.OAuthAccount;
import com.barofarm.auth.domain.oauth.OAuthProvider;
import java.util.Optional;
import java.util.UUID;

/**
 * OAuth 연결 정보를 저장/조회하는 포트.
 * 도메인 규칙은 애플리케이션 계층에서 유지한다.
 */
public interface OAuthAccountRepository {

    Optional<OAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    Optional<OAuthAccount> findByProviderAndUserId(OAuthProvider provider, UUID userId);

    void deleteAllByUserId(UUID userId);

    OAuthAccount save(OAuthAccount account);
}
