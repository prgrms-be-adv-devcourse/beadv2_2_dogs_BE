package com.barofarm.auth.domain.credential;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AuthCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private Long userId; // FK 아니고 id 들고 있는 상태
    private String loginEmail;
    private String passwordHash;

    protected AuthCredential() {
    }

    private AuthCredential(Long userId, String loginEmail, String passwordHash) {
        this.userId = userId;
        this.loginEmail = loginEmail;
        this.passwordHash = passwordHash;
    }

    public static AuthCredential create(Long userId, String loginEmail, String password) {
        return new AuthCredential(userId, loginEmail, password);
    }

    public int getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getLoginEmail() {
        return loginEmail;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
