package com.barofarm.auth.domain.oauth;

import com.barofarm.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
@Entity
@Table(
    name = "oauth_account",
    uniqueConstraints = {
        @jakarta.persistence.UniqueConstraint(
            name = "uk_oauth_provider_user",
            columnNames = {"provider", "provider_user_id"}
        )
    }
)
public class OAuthAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 128)
    private String providerUserId;

    @Column(name = "email", length = 320)
    private String email;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    protected OAuthAccount() {
    }

    private OAuthAccount(UUID userId, OAuthProvider provider, String providerUserId,
            String email, String name, String phone) {
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
        this.name = name;
        this.phone = phone;
    }

    public static OAuthAccount link(UUID userId, OAuthUserInfo userInfo) {
        // 공급자에서 내려준 정보를 고정된 도메인 모델로 캡슐화한다.
        return new OAuthAccount(
            userId,
            userInfo.provider(),
            userInfo.providerUserId(),
            userInfo.email(),
            userInfo.name(),
            userInfo.phone()
        );
    }

    public void touchLastLogin(LocalDateTime now) {
        // 마지막 로그인 시각만 갱신해서 변경 범위를 최소화한다.
        this.lastLoginAt = now;
    }
}
