package com.barofarm.auth.infrastructure.jpa;

import com.barofarm.auth.domain.credential.AuthCredential;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthCredentialJpaRepository extends JpaRepository<AuthCredential, UUID> {

    boolean existsByLoginEmail(String loginEmail);

    Optional<AuthCredential> findByLoginEmail(String loginEmail);
}
