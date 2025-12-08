package com.barofarm.auth.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String name;
    private String phone;
    private boolean marketingConsent;

    // 생성자 보호
    protected User() {
    }

    private User(String email, String name, String phone, boolean marketingConsent) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.marketingConsent = false;
    }

    // 팩토리 패턴?
    public static User create(String email, String name, String phone, boolean marketingConsent) {
        return new User(email, name, phone, marketingConsent);
    }

    // getter만 만들어 놓기 일단
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isMarketingConsent() {
        return marketingConsent;
    }
}
