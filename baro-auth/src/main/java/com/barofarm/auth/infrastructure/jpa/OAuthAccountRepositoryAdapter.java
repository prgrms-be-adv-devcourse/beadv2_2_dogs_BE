package com.barofarm.auth.infrastructure.jpa;

import com.barofarm.auth.application.port.out.OAuthAccountRepository;
import com.barofarm.auth.domain.oauth.OAuthAccount;
import com.barofarm.auth.domain.oauth.OAuthProvider;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OAuthAccountRepositoryAdapter implements OAuthAccountRepository {

    private final OAuthAccountJpaRepository repository;

    @Override
    public Optional<OAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId) {
        return repository.findByProviderAndProviderUserId(provider, providerUserId);
    }

    @Override
    public Optional<OAuthAccount> findByProviderAndUserId(OAuthProvider provider, UUID userId) {
        return repository.findByProviderAndUserId(provider, userId);
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        repository.deleteAllByUserId(userId);
    }

    @Override
    public OAuthAccount save(OAuthAccount account) {
        return repository.save(account);
    }
}
