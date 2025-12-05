package com.barofarm.auth.api;

// signup, login, /me 등 인증 관련 API

import com.barofarm.auth.api.dto.SignupRequest;
import com.barofarm.auth.application.AuthService;
import com.barofarm.auth.application.dto.SignUpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<SignUpResponse> signup(@RequestBody SignupRequest request) {
    var response = authService.signUp(request.toServiceRequest());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
