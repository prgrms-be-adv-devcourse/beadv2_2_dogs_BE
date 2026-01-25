package com.barofarm.auth.presentation;

import com.barofarm.auth.application.AuthService;
import com.barofarm.auth.application.usecase.LoginResult;
import com.barofarm.auth.application.usecase.SignUpResult;
import com.barofarm.auth.application.usecase.TokenResult;
import com.barofarm.auth.domain.user.User;
import com.barofarm.auth.exception.AuthErrorCode;
import com.barofarm.auth.infrastructure.security.AuthUserPrincipal;
import com.barofarm.auth.infrastructure.security.JwtTokenProvider;
import com.barofarm.auth.presentation.dto.admin.AdminUserSummaryResponse;
import com.barofarm.auth.presentation.dto.admin.UpdateSellerStatusRequest;
import com.barofarm.auth.presentation.dto.admin.UpdateUserStateRequest;
import com.barofarm.auth.presentation.dto.login.LoginRequest;
import com.barofarm.auth.presentation.dto.password.PasswordChangeRequest;
import com.barofarm.auth.presentation.dto.password.PasswordResetConfirmRequest;
import com.barofarm.auth.presentation.dto.password.PasswordResetRequest;
import com.barofarm.auth.presentation.dto.signup.SignupRequest;
import com.barofarm.auth.presentation.dto.token.AuthTokenResponse;
import com.barofarm.auth.presentation.dto.token.LogoutRequest;
import com.barofarm.auth.presentation.dto.token.RefreshTokenRequest;
import com.barofarm.auth.presentation.dto.user.MeResponse;
import com.barofarm.auth.presentation.dto.user.WithdrawRequest;
import com.barofarm.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
//public class AuthController implements AuthSwaggerApi {
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.barofarm.auth.infrastructure.config.AuthCookieProperties cookieProperties;

    @PostMapping("/signup")
    public ResponseEntity<AuthTokenResponse> signup(@RequestBody SignupRequest request) {
        SignUpResult response = authService.signUp(request.toServiceRequest());
        // [1] 토큰을 응답 바디가 아닌 HttpOnly 쿠키로 내려 XSS 접근을 차단한다.
        HttpHeaders headers = buildAuthCookies(response.accessToken(), response.refreshToken());
        AuthTokenResponse body = new AuthTokenResponse(response.userId(), response.email());
        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@RequestBody LoginRequest request) {
        LoginResult response = authService.login(request.toServiceRequest());
        // [2] 이메일 로그인도 동일한 쿠키 발급 흐름을 사용한다.
        HttpHeaders headers = buildAuthCookies(response.accessToken(), response.refreshToken());
        AuthTokenResponse body = new AuthTokenResponse(response.userId(), response.email());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @PostMapping("/password/reset/request")
    public ResponseEntity<Void> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/reset/confirm")
    public ResponseEntity<Void> resetPassword(@RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request.toServiceRequest());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/change")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal AuthUserPrincipal principal,
            @RequestBody PasswordChangeRequest request) {
        authService.changePassword(principal.getUserId(), request.toServiceRequest());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(
        @RequestBody(required = false) RefreshTokenRequest request
    ) {
        // [3] refresh 토큰은 쿠키에서 읽되, 구형 클라이언트(body 전달)도 허용한다.
        String refreshToken = resolveRefreshToken(request, true);
        TokenResult response = authService.refresh(refreshToken);
        HttpHeaders headers = buildAuthCookies(response.accessToken(), response.refreshToken());
        AuthTokenResponse body = new AuthTokenResponse(response.userId(), response.email());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @RequestBody(required = false) LogoutRequest request
    ) {
        String refreshToken = resolveRefreshToken(
            request == null ? null : new RefreshTokenRequest(request.refreshToken()),
            false
        );
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        HttpHeaders headers = clearAuthCookies();
        return ResponseEntity.ok().headers(headers).build();
    }

    // TODO: sercurity로 따로 관리하는 서비스 향후 AuthService와 관계 고려
    @GetMapping("/me")
    public ResponseEntity<MeResponse> getCurrentUser(@AuthenticationPrincipal AuthUserPrincipal principal) {

        MeResponse response = new MeResponse(
                principal.getUserId(),
                principal.getEmail(),
                principal.getName(),
                principal.getPhone(),
                principal.isMarketingConsent(),
                principal.getRole());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/withdraw")
    public ResponseEntity<Void> withdraw(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @RequestBody(required = false) WithdrawRequest request
    ) {
        // [1] 요청 바디가 없어도 처리 가능하도록 null reason 허용.
        authService.withdrawUser(
            principal.getUserId(),
            request == null ? null : request.toServiceRequest()
        );
        HttpHeaders headers = clearAuthCookies();
        return ResponseEntity.ok().headers(headers).build();
    }

    // ==== Seller와 관련된 부분
    @PostMapping("/{userId}/grant-seller")
    public ResponseEntity<Void> grantSeller(@PathVariable UUID userId) {
        authService.grantSeller(userId);
        return ResponseEntity.ok().build();
    }

    // ==== Admin-only seller status updates
    @PostMapping("/sellers/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateSellerStatus(
        @PathVariable UUID userId,
        @RequestBody UpdateSellerStatusRequest request
    ) {
        authService.updateSellerStatus(userId, request.sellerStatus(), request.reason());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminUserSummaryResponse>> getAdminUsers(
        @RequestParam(required = false) User.UserType type,
        @RequestParam(required = false) User.UserState state,
        Pageable pageable
    ) {
        Page<AdminUserSummaryResponse> response = authService.getAdminUsers(type, state, pageable);
        return ResponseEntity.ok(response);
    }

    // ==== Admin-only account state updates
    @PostMapping("/{userId}/state")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserState(
        @PathVariable UUID userId,
        @RequestBody UpdateUserStateRequest request
    ) {
        authService.updateUserState(userId, request.userState(), request.reason());
        return ResponseEntity.ok().build();
    }

    private HttpHeaders buildAuthCookies(String accessToken, String refreshToken) {
        // [4] Secure/SameSite 등 쿠키 속성은 설정으로 분리해 일관성 있게 적용한다.
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, CookieUtil.accessTokenCookie(
            accessToken, jwtTokenProvider.getAccessTokenValidity(), cookieProperties).toString());
        headers.add(HttpHeaders.SET_COOKIE, CookieUtil.refreshTokenCookie(
            refreshToken, jwtTokenProvider.getRefreshTokenValidity(), cookieProperties).toString());
        return headers;
    }

    private HttpHeaders clearAuthCookies() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, CookieUtil.clearAccessToken(cookieProperties).toString());
        headers.add(HttpHeaders.SET_COOKIE, CookieUtil.clearRefreshToken(cookieProperties).toString());
        return headers;
    }

    private String resolveRefreshToken(RefreshTokenRequest request, boolean required) {
        if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
            return request.refreshToken();
        }
        HttpServletRequest current = currentRequest();
        String cookieValue = CookieUtil.getCookieValue(
            current == null ? null : current.getCookies(), cookieProperties.getRefreshName());
        if (cookieValue == null || cookieValue.isBlank()) {
            if (!required) {
                return null;
            }
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        return cookieValue;
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }
}
