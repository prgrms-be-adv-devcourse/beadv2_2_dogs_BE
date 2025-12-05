package com.barofarm.auth.api;

// signup, login, /me 등이 들어갈 예정

import com.barofarm.auth.application.AuthService;
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
  public ResponseEntity<AuthService.SignUpResponse> signup(@RequestBody SignupRequest request) {
    var response = authService.signUp(request.toServiceRequest());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // API용 DTO → Service DTO 변환
  public record SignupRequest(
      String email, String password, String name, String phone, boolean marketingConsent) {
    public AuthService.SignUpRequest toServiceRequest() {
      return new AuthService.SignUpRequest(email, password, name, phone, marketingConsent);
    }
  }
}
