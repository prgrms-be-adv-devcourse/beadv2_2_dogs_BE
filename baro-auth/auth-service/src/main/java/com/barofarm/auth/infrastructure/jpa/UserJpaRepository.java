package com.barofarm.auth.infrastructure.jpa;

import com.barofarm.auth.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {}
