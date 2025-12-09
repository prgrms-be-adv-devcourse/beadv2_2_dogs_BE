package com.barofarm.auth.presentation.api;

import com.barofarm.auth.application.usecase.LoginResult;
import com.barofarm.auth.application.usecase.SignUpResult;
import com.barofarm.auth.application.usecase.TokenResult;
import com.barofarm.auth.infrastructure.security.AuthUserPrincipal;
import com.barofarm.auth.presentation.dto.login.LoginRequest;
import com.barofarm.auth.presentation.dto.password.PasswordChangeRequest;
import com.barofarm.auth.presentation.dto.password.PasswordResetConfirmRequest;
import com.barofarm.auth.presentation.dto.password.PasswordResetRequest;
import com.barofarm.auth.presentation.dto.signup.SignupRequest;
import com.barofarm.auth.presentation.dto.token.LogoutRequest;
import com.barofarm.auth.presentation.dto.token.RefreshTokenRequest;
import com.barofarm.auth.presentation.dto.user.MeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "회원가입 / 로그인 / 내 정보 조회")
public interface AuthSwaggerApi {

    @Operation(summary = "회원가입", description = "이메일/비밀번호/이름/전화번호/마케팅 동의로 사용자 생성")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "생성됨"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")})
    ResponseEntity<SignUpResult> signup(SignupRequest request);

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하고 토큰 발급")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")})
    ResponseEntity<LoginResult> login(LoginRequest request);

    @Operation(summary = "비밀번호 재설정 코드 발송", description = "이메일로 비밀번호 재설정 코드 전송")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "코드 발송 성공"),
            @ApiResponse(responseCode = "404", description = "이메일 없음")})
    ResponseEntity<Void> requestPasswordReset(PasswordResetRequest request);

    @Operation(summary = "비밀번호 재설정 완료", description = "코드 검증 후 새 비밀번호로 변경")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "재설정 성공"),
            @ApiResponse(responseCode = "400", description = "코드 오류/만료"),
            @ApiResponse(responseCode = "404", description = "이메일 없음")})
    ResponseEntity<Void> resetPassword(PasswordResetConfirmRequest request);

    @Operation(summary = "비밀번호 변경", description = "로그인된 사용자가 현재/새 비밀번호로 변경")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "401", description = "현재 비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "자격 증명 없음")})
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Void> changePassword(@Parameter(hidden = true) AuthUserPrincipal principal,
            PasswordChangeRequest request);

    @Operation(summary = "리프레시 토큰 재발급", description = "리프레시 토큰으로 액세스/리프레시 토큰을 재발급")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰 오류")})
    ResponseEntity<TokenResult> refresh(RefreshTokenRequest request);

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 폐기하여 로그아웃 처리")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "로그아웃 성공")})
    ResponseEntity<Void> logout(LogoutRequest request);

    @Operation(summary = "내 정보 조회", description = "현재 인증된 사용자 정보 반환")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")})
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<MeResponse> getCurrentUser(@Parameter(hidden = true) AuthUserPrincipal principal);

    @Operation(summary = "판매자 권한 부여", description = "관리자/시스템용 seller 권한 부여")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")})
    ResponseEntity<Void> grantSeller(UUID userId);
}
