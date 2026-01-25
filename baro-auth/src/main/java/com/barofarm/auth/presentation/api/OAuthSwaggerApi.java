//package com.barofarm.auth.presentation.api;
//
//import com.barofarm.auth.application.usecase.OAuthLinkStartResult;
//import com.barofarm.auth.application.usecase.OAuthLoginStateResult;
//import com.barofarm.auth.infrastructure.security.AuthUserPrincipal;
//import com.barofarm.auth.presentation.dto.oauth.OAuthCallbackRequest;
//import com.barofarm.auth.presentation.dto.oauth.OAuthLinkCallbackRequest;
//import com.barofarm.auth.presentation.dto.token.AuthTokenResponse;
//// import io.swagger.v3.oas.annotations.Operation;
//// import io.swagger.v3.oas.annotations.Parameter;
//// import io.swagger.v3.oas.annotations.responses.ApiResponse;
//// import io.swagger.v3.oas.annotations.responses.ApiResponses;
//// import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//// import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.http.ResponseEntity;
//
//// @Tag(name = "OAuth", description = "소셜 로그인/계정 연결 API")
//public interface OAuthSwaggerApi {
//
////     @Operation(summary = "OAuth state 발급", description = "소셜 로그인 시작 단계에서 CSRF 방어용 state를 발급한다.")
////     @ApiResponses({@ApiResponse(responseCode = "200", description = "발급 성공")})
//    ResponseEntity<OAuthLoginStateResult> issueLoginState();
//
////     @Operation(summary = "소셜 로그인 콜백", description = "code/state로 사용자 정보를 조회하고 JWT를 발급한다.")
////     @ApiResponses({
////         @ApiResponse(responseCode = "200", description = "로그인 성공"),
////         @ApiResponse(responseCode = "401", description = "state 검증 실패"),
////         @ApiResponse(responseCode = "409", description = "link_required")
////     })
//    ResponseEntity<AuthTokenResponse> oauthCallback(OAuthCallbackRequest request);
//
////     @Operation(summary = "계정 연결 시작", description = "로그인된 사용자에게 연결용 state를 발급한다.")
////     @ApiResponses({@ApiResponse(responseCode = "200", description = "발급 성공")})
////     @SecurityRequirement(name = "bearerAuth")
//    ResponseEntity<OAuthLinkStartResult> startLink(@Parameter(hidden = true) AuthUserPrincipal principal);
//
////     @Operation(summary = "계정 연결 콜백", description = "code/state로 소셜 계정을 현재 사용자에 연결한다.")
////     @ApiResponses({
////         @ApiResponse(responseCode = "200", description = "연결 성공"),
////         @ApiResponse(responseCode = "401", description = "state 검증 실패"),
////         @ApiResponse(responseCode = "409", description = "이미 다른 계정에 연결됨")
////     })
////     @SecurityRequirement(name = "bearerAuth")
//    ResponseEntity<Void> linkCallback(
////         @Parameter(hidden = true) AuthUserPrincipal principal,
//        OAuthLinkCallbackRequest request
//    );
//}
