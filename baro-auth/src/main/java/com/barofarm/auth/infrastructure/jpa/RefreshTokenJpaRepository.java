package com.barofarm.auth.infrastructure.jpa;

import com.barofarm.auth.domain.token.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteAllByUserId(Long userId);
}
