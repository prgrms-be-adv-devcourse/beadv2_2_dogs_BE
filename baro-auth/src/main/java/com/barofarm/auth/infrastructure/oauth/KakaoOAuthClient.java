package com.barofarm.auth.infrastructure.oauth;

import com.barofarm.auth.domain.oauth.OAuthProvider;
import com.barofarm.auth.domain.oauth.OAuthUserInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class KakaoOAuthClient implements OAuthProviderHandler {

    private final KakaoOAuthProperties properties;
    private final RestClient restClient;

    public KakaoOAuthClient(KakaoOAuthProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public OAuthUserInfo fetchUserInfo(String code, String state) {
        // 1) 인가 코드로 액세스 토큰 발급
        TokenResponse token = requestAccessToken(code);
        // 2) 액세스 토큰으로 사용자 정보 조회
        KakaoUserInfoResponse userInfo = requestUserInfo(token.access_token());

        KakaoAccount account = userInfo.kakao_account();
        String email = account == null ? null : account.email();
        String phone = account == null ? null : account.phone_number();
        String name = resolveName(account);

        return new OAuthUserInfo(
            OAuthProvider.KAKAO,
            String.valueOf(userInfo.id()),
            email,
            name,
            phone
        );
    }

    private TokenResponse requestAccessToken(String code) {
        // 카카오 토큰 발급은 form-urlencoded POST 바디로 전달한다.
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.getClientId());
        form.add("code", code);
        form.add("redirect_uri", properties.getRedirectUri());
        if (StringUtils.hasText(properties.getClientSecret())) {
            form.add("client_secret", properties.getClientSecret());
        }

        return restClient.post()
            .uri(properties.getTokenUri())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(TokenResponse.class);
    }

    private KakaoUserInfoResponse requestUserInfo(String accessToken) {
        // Authorization 헤더에 Bearer 토큰을 붙여 호출한다.
        return restClient.get()
            .uri(properties.getUserInfoUri())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .retrieve()
            .body(KakaoUserInfoResponse.class);
    }

    private String resolveName(KakaoAccount account) {
        if (account == null) {
            return null;
        }
        if (StringUtils.hasText(account.name())) {
            return account.name();
        }
        KakaoProfile profile = account.profile();
        return profile == null ? null : profile.nickname();
    }

    private record TokenResponse(
        String access_token,
        String token_type,
        String refresh_token,
        int expires_in,
        String scope
    ) {
    }

    private record KakaoUserInfoResponse(
        long id,
        KakaoAccount kakao_account
    ) {
    }

    private record KakaoAccount(
        String email,
        String phone_number,
        String name,
        KakaoProfile profile
    ) {
    }

    private record KakaoProfile(
        String nickname
    ) {
    }
}
