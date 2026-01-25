package com.barofarm.auth.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth.cookie")
public class AuthCookieProperties {

    // [1] 쿠키 이름/옵션을 설정으로 분리해 환경별 보안 정책을 통일한다.
    private String accessName = "access_token";
    private String refreshName = "refresh_token";
    private String path = "/";
    private String domain;
    // [2] Secure=true는 HTTPS에서만 쿠키를 전송해 네트워크 탈취를 줄인다.
    private boolean secure = true;
    // [3] SameSite는 CSRF 방어에 유효하며, Strict/Lax/None 정책을 환경에 맞게 선택한다.
    private String sameSite = "Strict";

    public String getAccessName() {
        return accessName;
    }

    public void setAccessName(String accessName) {
        this.accessName = accessName;
    }

    public String getRefreshName() {
        return refreshName;
    }

    public void setRefreshName(String refreshName) {
        this.refreshName = refreshName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getSameSite() {
        return sameSite;
    }

    public void setSameSite(String sameSite) {
        this.sameSite = sameSite;
    }
}
