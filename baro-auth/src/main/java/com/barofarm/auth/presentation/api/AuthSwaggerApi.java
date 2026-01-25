//package com.barofarm.auth.presentation.api;
//
//import com.barofarm.auth.domain.user.User;
//import com.barofarm.auth.infrastructure.security.AuthUserPrincipal;
//import com.barofarm.auth.presentation.dto.admin.AdminUserSummaryResponse;
//import com.barofarm.auth.presentation.dto.admin.UpdateSellerStatusRequest;
//import com.barofarm.auth.presentation.dto.admin.UpdateUserStateRequest;
//import com.barofarm.auth.presentation.dto.login.LoginRequest;
//import com.barofarm.auth.presentation.dto.password.PasswordChangeRequest;
//import com.barofarm.auth.presentation.dto.password.PasswordResetConfirmRequest;
//import com.barofarm.auth.presentation.dto.password.PasswordResetRequest;
//import com.barofarm.auth.presentation.dto.signup.SignupRequest;
//import com.barofarm.auth.presentation.dto.token.AuthTokenResponse;
//import com.barofarm.auth.presentation.dto.token.LogoutRequest;
//import com.barofarm.auth.presentation.dto.token.RefreshTokenRequest;
//import com.barofarm.auth.presentation.dto.user.MeResponse;
//import com.barofarm.auth.presentation.dto.user.WithdrawRequest;
//// import io.swagger.v3.oas.annotations.Operation;
//// import io.swagger.v3.oas.annotations.Parameter;
//// import io.swagger.v3.oas.annotations.responses.ApiResponse;
//// import io.swagger.v3.oas.annotations.responses.ApiResponses;
//// import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//// import io.swagger.v3.oas.annotations.tags.Tag;
//import java.util.UUID;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.ResponseEntity;
//
//// @Tag(name = "Auth", description = "회원가입/로그인/내 정보 조회")
//public interface AuthSwaggerApi {
//
////     @Operation(summary = "회원가입", description = "이메일/비밀번호/이름/전화번호/마케팅 동의로 사용자 생성")
////     @ApiResponses({
////         @ApiResponse(responseCode = "201", description = "생성됨"),
////         @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
////         @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
////     })
//    ResponseEntity<AuthTokenResponse> signup(SignupRequest request);
//
////     @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하고 토큰 발급")
////     @ApiResponses({
////         @ApiResponse(responseCode = "200", description = "로그인 성공"),
////         @ApiResponse(responseCode = "401", description = "인증 실패")
////     })
//    ResponseEntity<AuthTokenResponse> login(LoginRequest request);
//
////     @Operation(summary = "비밀번호 재설정 코드 발송", description = "이메일로 비밀번호 재설정 코드를 전송")
////     @ApiResponses({
////         @ApiResponse(responseCode = "200", description = "코드 발송 성공"),
////         @ApiResponse(responseCode = "404", description = "이메일 없음")
////     })
//    ResponseEntity<Void> requestPasswordReset(PasswordResetRequest request);
//
////     @Operation(summary = "비밀번호 재설정 완료", description = "코드 검증 후 새 비밀번호로 변경")
////     @ApiResponses({
////         @ApiResponse(responseCode = "200", description = "재설정 성공"),
////         @ApiResponse(responseCode = "400", description = "코드 오류/만료"),
////         @ApiResponse(responseCode = "404", description = "이메일 없음")
////     })
//    ResponseEntity<Void> resetPassword(PasswordResetConfirmRequest request);
//
////     @Operation(summary = "비밀번호 변경", description = "로그인된 사용자 비밀번호 변경")
////     @ApiResponses({
////         @ApiResponse(responseCode = "200", description = "변경 성공"),
////         @ApiResponse(responseCode = "401", description = "현재 비밀번호 불일치"),
////         @ApiResponse(responseCode = "404", description = "계정 정보 없음")
////     })
////     @SecurityRequirement(name = "bearerAuth")
//    ResponseEntity<Void> changePassword(
////         @Parameter(hidden = true) AuthUserPrincipal principal,
//        PasswordChangeRequest request
//    );
//
////     @Operation(summary = "리프레시 토큰 재발급", description = "리프레시 토큰으로 새 액세스/리프레시 토큰 발급")
////     @ApiResponses({
////         @ApiResponse(responseCode = "200", description = "재발급 성공"),
////         @ApiResponse(responseCode = "401", description = "리프레시 토큰 오류")
////     })
//    ResponseEntity<AuthTokenResponse> refresh(RefreshTokenRequest request);
//
////     @Operation(summary = "로그아웃", description = "리프레시 토큰을 폐기하여 로그아웃 처리")
////     @ApiResponses({
////         @ApiResponse(responseCode = "200", description = "로그아웃 성공")
////     })
//    ResponseEntity<Void> logout(LogoutRequest request);
//
////     @Operation(summary = "내 정보 조회", description = "현재 인증된 사용자 정보 반환")
////     @ApiResponses({
////         @ApiResponse(responseCode = "200", description = "성공"),
////         @ApiResponse(responseCode = "401", description = "인증 필요")
////     })
////     @SecurityRequirement(name = "bearerAuth")
//    ResponseEntity<MeResponse> getCurrentUser(@Parameter(hidden = true) AuthUserPrincipal principal);
//
////     @Operation(summary = "회원 탈퇴", description = "계정을 탈퇴 처리하고 인증 정보를 폐기")
////     @ApiResponses({
////         @ApiResponse(responseCode = "200", description = "성공"),
////         @ApiResponse(responseCode = "401", description = "인증 필요")
////     })
////     @SecurityRequirement(name = "bearerAuth")
//    ResponseEntity<Void> withdraw(@Parameter(hidden = true) AuthUserPrincipal principal, WithdrawRequest request);
//
////     @Operation(summary = "판매자 권한 부여", description = "관리자/시스템용 판매자 권한 부여")
////     @ApiResponses({
////         @ApiResponse(responseCode = "200", description = "성공"),
////         @ApiResponse(responseCode = "404", description = "사용자 없음")
////     })
//    ResponseEntity<Void> grantSeller(UUID userId);
//
////     @Operation(
////         summary = "판매자 상태 변경",
////         description = "관리자 권한으로 판매자 상태를 변경합니다(APPROVED/REJECTED/SUSPENDED)"
////     )
////     @ApiResponses({
////         @ApiResponse(responseCode = "200", description = "성공"),
////         @ApiResponse(responseCode = "404", description = "사용자 없음")
////     })
////     @SecurityRequirement(name = "bearerAuth")
//    ResponseEntity<Void> updateSellerStatus(UUID userId, UpdateSellerStatusRequest request);
//
////     @Operation(summary = "사용자 상태 변경", description = "관리자 권한으로 계정 상태 변경(ACTIVE/SUSPENDED/BLOCKED)")
////     @ApiResponses({
////         @ApiResponse(responseCode = "200", description = "성공"),
////         @ApiResponse(responseCode = "404", description = "사용자 없음")
////     })
////     @SecurityRequirement(name = "bearerAuth")
//    ResponseEntity<Void> updateUserState(UUID userId, UpdateUserStateRequest request);
//
////     @Operation(summary = "관리자 사용자 목록 조회", description = "관리자 전용 사용자 목록 조회(필터: userType/userState)")
////     @ApiResponses({
////         @ApiResponse(responseCode = "200", description = "성공"),
////         @ApiResponse(responseCode = "401", description = "인증 필요"),
////         @ApiResponse(responseCode = "403", description = "권한 없음")
////     })
////     @SecurityRequirement(name = "bearerAuth")
//    ResponseEntity<Page<AdminUserSummaryResponse>> getAdminUsers(
//        User.UserType type,
//        User.UserState state,
//        Pageable pageable
//    );
//}
