package com.barofarm.auth.domain.credential;

import com.barofarm.auth.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;

@Getter
@Entity
@Table(name = "auth_credential")
public class AuthCredential extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(name = "login_email", nullable = false, length = 320)
    private String loginEmail;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "salt", nullable = false, length = 64)
    private String salt;

    protected AuthCredential() {
    }

    private AuthCredential(UUID userId, String loginEmail, String passwordHash, String salt) {
        this.userId = userId;
        this.loginEmail = loginEmail;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public static AuthCredential create(UUID userId, String loginEmail, String passwordHash, String salt) {
        return new AuthCredential(userId, loginEmail, passwordHash, salt);
    }

    public void changePassword(String passwordHash, String salt) {
        this.passwordHash = passwordHash;
        this.salt = salt;
    }
}
