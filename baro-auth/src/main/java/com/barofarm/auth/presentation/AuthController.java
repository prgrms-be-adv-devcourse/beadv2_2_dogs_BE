package com.barofarm.auth.presentation;

import com.barofarm.auth.application.AuthService;
import com.barofarm.auth.application.usecase.LoginResult;
import com.barofarm.auth.application.usecase.SignUpResult;
import com.barofarm.auth.application.usecase.TokenResult;
import com.barofarm.auth.infrastructure.security.AuthUserPrincipal;
import com.barofarm.auth.presentation.api.AuthSwaggerApi;
import com.barofarm.auth.presentation.dto.login.LoginRequest;
import com.barofarm.auth.presentation.dto.password.PasswordChangeRequest;
import com.barofarm.auth.presentation.dto.password.PasswordResetConfirmRequest;
import com.barofarm.auth.presentation.dto.password.PasswordResetRequest;
import com.barofarm.auth.presentation.dto.signup.SignupRequest;
import com.barofarm.auth.presentation.dto.token.LogoutRequest;
import com.barofarm.auth.presentation.dto.token.RefreshTokenRequest;
import com.barofarm.auth.presentation.dto.user.MeResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthSwaggerApi {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResult> signup(@RequestBody SignupRequest request) {
        var response = authService.signUp(request.toServiceRequest());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResult> login(@RequestBody LoginRequest request) {
        var response = authService.login(request.toServiceRequest());
        return ResponseEntity.ok(response);
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
    public ResponseEntity<TokenResult> refresh(@RequestBody RefreshTokenRequest request) {
        TokenResult response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> getCurrentUser(@AuthenticationPrincipal AuthUserPrincipal principal) {

        MeResponse response = new MeResponse(principal.getUserId(), principal.getUsername(), principal.getRole());

        return ResponseEntity.ok(response);
    }

    // ==== Seller와 관련된 부분
    @PostMapping("/{userId}/grant-seller")
    public ResponseEntity<Void> grantSeller(@PathVariable UUID userId) {
        authService.grantSeller(userId);
        return ResponseEntity.ok().build();
    }
}
