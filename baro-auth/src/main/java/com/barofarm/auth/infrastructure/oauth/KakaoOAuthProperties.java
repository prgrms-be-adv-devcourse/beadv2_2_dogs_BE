package com.barofarm.auth.infrastructure.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.kakao")
public class KakaoOAuthProperties {

    // REST API 키 (카카오 디벨로퍼스 -> 내 애플리케이션)
    private String clientId;
    // 클라이언트 시크릿 (선택, 사용 설정 시만 필요)
    private String clientSecret;
    // 카카오 로그인 콜백 URL
    private String redirectUri;
    // 토큰 발급 엔드포인트
    private String tokenUri;
    // 사용자 정보 조회 엔드포인트
    private String userInfoUri;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public void setTokenUri(String tokenUri) {
        this.tokenUri = tokenUri;
    }

    public String getUserInfoUri() {
        return userInfoUri;
    }

    public void setUserInfoUri(String userInfoUri) {
        this.userInfoUri = userInfoUri;
    }
}
