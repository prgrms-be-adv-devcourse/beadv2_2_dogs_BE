package com.barofarm.auth.domain.user;

import com.barofarm.auth.common.entity.BaseEntity;
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

    @Column(name = "email", nullable = false, length = 320, unique = true)
    private String email;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Column(name = "marketing_consent", nullable = false)
    private boolean marketingConsent = false; // false로 초기화

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType;

    // 생성자 보호
    protected User() {
    }

    private User(String email, String name, String phone, boolean marketingConsent) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.marketingConsent = marketingConsent;
        this.userType = UserType.CUSTOMER;
    }

    // 팩토리 패턴?
    public static User create(String email, String name, String phone, boolean marketingConsent) {
        return new User(email, name, phone, marketingConsent);
    }

    public void changeToSeller() {
        this.userType = UserType.SELLER;
    }

    public enum UserType {
        CUSTOMER,
        SELLER,
        ADMIN
    }
}
