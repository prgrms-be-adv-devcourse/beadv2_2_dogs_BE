package com.barofarm.auth.infrastructure.oauth;

import com.barofarm.auth.domain.oauth.OAuthProvider;
import com.barofarm.auth.domain.oauth.OAuthUserInfo;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class NaverOAuthClient implements OAuthProviderHandler {

    private final NaverOAuthProperties properties;
    private final RestClient restClient;

    public NaverOAuthClient(NaverOAuthProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.NAVER;
    }

    @Override
    public OAuthUserInfo fetchUserInfo(String code, String state) {
        // 1) 인가 코드로 액세스 토큰 발급
        TokenResponse token = requestAccessToken(code, state);
        // 2) 액세스 토큰으로 사용자 정보 조회
        NaverUserInfoResponse userInfo = requestUserInfo(token.access_token());

        return new OAuthUserInfo(
            OAuthProvider.NAVER,
            userInfo.response().id(),
            userInfo.response().email(),
            userInfo.response().name(),
            userInfo.response().mobile()
        );
    }

    private TokenResponse requestAccessToken(String code, String state) {
        // 네이버는 쿼리 파라미터 기반으로 토큰 발급을 처리한다.
        URI uri = UriComponentsBuilder.fromUriString(properties.getTokenUri())
            .queryParam("grant_type", "authorization_code")
            .queryParam("client_id", properties.getClientId())
            .queryParam("client_secret", properties.getClientSecret())
            .queryParam("code", code)
            .queryParam("state", state)
            .queryParam("redirect_uri", properties.getRedirectUri())
            .build(true)
            .toUri();

        return restClient.post()
            .uri(uri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .retrieve()
            .body(TokenResponse.class);
    }

    private NaverUserInfoResponse requestUserInfo(String accessToken) {
        // Authorization 헤더에 Bearer 토큰을 붙여 호출한다.
        return restClient.get()
            .uri(properties.getUserInfoUri())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .retrieve()
            .body(NaverUserInfoResponse.class);
    }

    private record TokenResponse(
        String access_token,
        String token_type,
        String refresh_token,
        int expires_in
    ) {
    }

    private record NaverUserInfoResponse(
        String resultcode,
        String message,
        NaverUser response
    ) {
    }

    private record NaverUser(
        String id,
        String email,
        String name,
        String mobile
    ) {
    }
}
