package com.barofarm.auth.api;

// signup, login, /me 등 인증 관련 API

import com.barofarm.auth.api.dto.LoginRequest;
import com.barofarm.auth.api.dto.MeResponse;
import com.barofarm.auth.api.dto.SignupRequest;
import com.barofarm.auth.application.AuthService;
import com.barofarm.auth.application.dto.LoginResult;
import com.barofarm.auth.application.dto.SignUpResult;
import com.barofarm.auth.infrastructure.security.AuthUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

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

  @GetMapping("/me")
  public ResponseEntity<MeResponse> getCurrentUser(
      @AuthenticationPrincipal AuthUserPrincipal principal) {

    // 여기에선 이미 SecurityFilterChain에서 인증 통과
    MeResponse response =
        new MeResponse(principal.getUserId(), principal.getUsername(), principal.getRole());

    return ResponseEntity.ok(response);
  }
}
