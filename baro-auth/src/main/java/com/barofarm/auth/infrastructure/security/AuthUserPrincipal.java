package com.barofarm.auth.infrastructure.security;

import com.barofarm.auth.domain.user.User;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthUserPrincipal implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String name;
    private final String phone;
    private final boolean marketingConsent;
    private final User.UserType role;

    public AuthUserPrincipal(UUID userId, String email, String name, String phone, boolean marketingConsent,
            User.UserType role) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.marketingConsent = marketingConsent;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
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

    public User.UserType getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return ""; // JWT 인증이라 비밀번호는 사용하지 않음
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
