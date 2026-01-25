package com.barofarm.auth.infrastructure.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 네이버 OAuth 연동 설정값.
 * 민감 정보는 환경 변수로 주입한다.
 */
@ConfigurationProperties(prefix = "oauth.naver")
public class NaverOAuthProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String tokenUri;
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
