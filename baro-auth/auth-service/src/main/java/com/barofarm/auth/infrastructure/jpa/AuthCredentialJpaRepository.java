package com.barofarm.auth.infrastructure.jpa;

import com.barofarm.auth.domain.credential.AuthCredential;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthCredentialJpaRepository extends JpaRepository<AuthCredential, Long> {

  boolean existsByLoginEmail(String loginEmail);
}
