package com.barofarm.auth.infrastructure.jpa;

import com.barofarm.auth.domain.verification.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerification,Long> {

    Optional<EmailVerification> findByEmailAndCodeAndVerifiedIsFalse(String email, String code);
    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);
    void deleteByEmail(String email);
}
