package com.barofarm.auth.presentation;

import com.barofarm.auth.application.AuthService;
import com.barofarm.auth.application.usecase.OAuthLinkStartResult;
import com.barofarm.auth.application.usecase.OAuthLoginStateResult;
import com.barofarm.auth.application.usecase.TokenResult;
import com.barofarm.auth.infrastructure.config.AuthCookieProperties;
import com.barofarm.auth.infrastructure.security.AuthUserPrincipal;
import com.barofarm.auth.infrastructure.security.JwtTokenProvider;
import com.barofarm.auth.presentation.dto.oauth.OAuthCallbackRequest;
import com.barofarm.auth.presentation.dto.oauth.OAuthLinkCallbackRequest;
import com.barofarm.auth.presentation.dto.token.AuthTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
//public class OAuthController implements OAuthSwaggerApi {
public class OAuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieProperties cookieProperties;

    @PostMapping("/oauth/callback")
    public ResponseEntity<AuthTokenResponse> oauthCallback(@RequestBody OAuthCallbackRequest request) {
        return handleCallback(request);
    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<AuthTokenResponse> oauthCallback(
        @RequestParam String provider,
        @RequestParam String code,
        @RequestParam String state
    ) {
        return handleCallback(new OAuthCallbackRequest(provider, code, state));
    }

    @PostMapping("/oauth/state")
    public ResponseEntity<OAuthLoginStateResult> issueLoginState() {
        OAuthLoginStateResult response = authService.startOAuthLogin();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/oauth/link/start")
    public ResponseEntity<OAuthLinkStartResult> startLink(@AuthenticationPrincipal AuthUserPrincipal principal) {
        OAuthLinkStartResult response = authService.startOAuthLink(principal.getUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/oauth/link/callback")
    public ResponseEntity<Void> linkCallback(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @RequestBody OAuthLinkCallbackRequest request
    ) {
        authService.oauthLinkCallback(request.toServiceRequest(principal.getUserId()));
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<AuthTokenResponse> handleCallback(OAuthCallbackRequest request) {
        TokenResult response = authService.oauthCallback(request.toServiceRequest());
        // [1] 소셜 로그인도 쿠키로 토큰을 발급해 XSS 저장소 노출을 피한다.
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, CookieUtil.accessTokenCookie(
            response.accessToken(), jwtTokenProvider.getAccessTokenValidity(), cookieProperties).toString());
        headers.add(HttpHeaders.SET_COOKIE, CookieUtil.refreshTokenCookie(
            response.refreshToken(), jwtTokenProvider.getRefreshTokenValidity(), cookieProperties).toString());
        AuthTokenResponse body = new AuthTokenResponse(response.userId(), response.email());
        return ResponseEntity.ok().headers(headers).body(body);
    }
}
