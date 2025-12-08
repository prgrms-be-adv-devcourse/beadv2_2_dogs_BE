package com.barofarm.auth.api;

import com.barofarm.auth.api.dto.LoginRequest;
import com.barofarm.auth.api.dto.LogoutRequest;
import com.barofarm.auth.api.dto.MeResponse;
import com.barofarm.auth.api.dto.RefreshTokenRequest;
import com.barofarm.auth.api.dto.SignupRequest;
import com.barofarm.auth.application.AuthService;
import com.barofarm.auth.application.usecase.LoginResult;
import com.barofarm.auth.application.usecase.SignUpResult;
import com.barofarm.auth.application.usecase.TokenResult;
import com.barofarm.auth.infrastructure.security.AuthUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Auth", description = "회원가입 / 로그인 / 내 정보 조회")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일/비밀번호/이름/전화번호/마케팅 동의로 사용자 생성")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "생성됨"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")})
    public ResponseEntity<SignUpResult> signup(@RequestBody SignupRequest request) {
        var response = authService.signUp(request.toServiceRequest());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하고 토큰 발급")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")})
    public ResponseEntity<LoginResult> login(@RequestBody LoginRequest request) {
        var response = authService.login(request.toServiceRequest());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "리프레시 토큰 재발급", description = "리프레시 토큰으로 액세스/리프레시 토큰을 재발급")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰 오류")})
    public ResponseEntity<TokenResult> refresh(@RequestBody RefreshTokenRequest request) {
        TokenResult response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "리프레시 토큰을 폐기하여 로그아웃 처리")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "로그아웃 성공")})
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Operation(summary = "내 프로필", description = "현재 인증된 사용자 정보를 반환")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")})
    public ResponseEntity<MeResponse> getCurrentUser(@AuthenticationPrincipal AuthUserPrincipal principal) {

        MeResponse response = new MeResponse(principal.getUserId(), principal.getUsername(), principal.getRole());

        return ResponseEntity.ok(response);
    }
}
