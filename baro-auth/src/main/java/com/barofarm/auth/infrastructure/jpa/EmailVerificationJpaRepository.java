package com.barofarm.auth.infrastructure.jpa;

import com.barofarm.auth.domain.verification.EmailVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmailAndCodeAndVerifiedIsFalse(String email, String code);

    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);

    void deleteByEmail(String email);

    void deleteAllByEmail(String email);
}
