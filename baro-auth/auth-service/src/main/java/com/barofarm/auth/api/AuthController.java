package com.barofarm.auth.api;

// signup, login, /me 등 인증 관련 API

import com.barofarm.auth.api.dto.LoginRequest;
import com.barofarm.auth.api.dto.MeResponse;
import com.barofarm.auth.api.dto.SignupRequest;
import com.barofarm.auth.application.AuthService;
import com.barofarm.auth.application.dto.LoginResult;
import com.barofarm.auth.application.dto.SignUpResult;
import com.barofarm.auth.infrastructure.security.AuthUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "회원 가입 / 로그인 / 내 정보 조회")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/signup")
  @Operation(summary = "회원 가입", description = "이메일/비밀번호/이름/전화번호/마케팅 동의로 회원을 생성합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "가입 성공",
        content = @Content(schema = @Schema(implementation = SignUpResult.class))),
    @ApiResponse(responseCode = "400", description = "유효성 오류"),
    @ApiResponse(responseCode = "409", description = "이미 가입된 이메일")
  })
  public ResponseEntity<SignUpResult> signup(@RequestBody SignupRequest request) {
    var response = authService.signUp(request.toServiceRequest());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하고 Access/Refresh 토큰을 발급합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "로그인 성공",
        content = @Content(schema = @Schema(implementation = LoginResult.class))),
    @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  public ResponseEntity<LoginResult> login(@RequestBody LoginRequest request) {
    var response = authService.login(request.toServiceRequest());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/me")
  @Operation(summary = "내 정보 조회", description = "현재 인증된 사용자의 기본 정보를 반환합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = MeResponse.class))),
    @ApiResponse(responseCode = "401", description = "인증 필요")
  })
  public ResponseEntity<MeResponse> getCurrentUser(
      @AuthenticationPrincipal AuthUserPrincipal principal) {

    // 여기에선 이미 SecurityFilterChain에서 인증 통과
    MeResponse response =
        new MeResponse(principal.getUserId(), principal.getUsername(), principal.getRole());

    return ResponseEntity.ok(response);
  }
}
