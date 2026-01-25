package com.barofarm.auth.infrastructure.jpa;

import com.barofarm.auth.domain.user.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Query("""
        select u
        from User u
        where (:type is null or u.userType = :type)
          and (:state is null or u.userState = :state)
        """)
    Page<User> findAdminUsers(
        @Param("type") User.UserType type,
        @Param("state") User.UserState state,
        Pageable pageable
    );
}
