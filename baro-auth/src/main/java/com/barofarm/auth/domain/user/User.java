package com.barofarm.auth.domain.user;

import com.barofarm.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;

@Getter
@Entity
@Table(name = "users")
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    // 소셜 로그인에서 이메일이 비어올 수 있어 nullable로 둔다.
    @Column(name = "email", length = 320, unique = true)
    private String email;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    // 네이버/카카오에서 전화번호 제공이 선택이므로 nullable로 둔다.
    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "marketing_consent", nullable = false)
    private boolean marketingConsent = false; // false로 초기화

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_state", nullable = false, length = 20)
    private UserState userState;

    // 마지막 로그인 시각은 인증 흐름에서만 갱신되도록 엔티티가 직접 관리한다.
    @Column(name = "last_login_at")
    private java.time.LocalDateTime lastLoginAt;

    // 생성자 보호
    protected User() {
    }

    private User(String email, String name, String phone, boolean marketingConsent) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.marketingConsent = marketingConsent;
        this.userType = UserType.CUSTOMER;
        this.userState = UserState.ACTIVE; // Account-level status used for gateway/OPA checks.
    }

    // 팩토리 패턴?
    public static User create(String email, String name, String phone, boolean marketingConsent) {
        return new User(email, name, phone, marketingConsent);
    }

    public void touchLastLogin(java.time.LocalDateTime now) {
        // 로그인 시각 갱신을 도메인 내부로 캡슐화한다.
        this.lastLoginAt = now;
    }

    public void changeToSeller() {
        this.userType = UserType.SELLER;
    }

    public void changeState(UserState userState) {
        if (userState != null) {
            this.userState = userState;
        }
    }

    public void withdraw(String anonymizedEmail, String anonymizedName) {
        this.userState = UserState.WITHDRAWN;
        this.email = anonymizedEmail;
        this.name = anonymizedName;
        this.phone = null;
        this.marketingConsent = false;
    }

    public enum UserType {
        CUSTOMER,
        SELLER,
        ADMIN
    }

    public enum UserState {
        ACTIVE,
        SUSPENDED,
        BLOCKED,
        WITHDRAWN
    }
}
