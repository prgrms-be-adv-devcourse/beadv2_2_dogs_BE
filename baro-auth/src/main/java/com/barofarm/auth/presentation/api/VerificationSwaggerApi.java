package com.barofarm.auth.presentation.api;

import com.barofarm.auth.presentation.dto.verification.SendCodeRequest;
import com.barofarm.auth.presentation.dto.verification.VerifyCodeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Email Verification", description = "이메일 인증코드 발송 및 확인")
public interface VerificationSwaggerApi {

    @Operation(summary = "인증코드 발송", description = "이메일로 인증코드를 전송합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "발송 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 이메일")})
    ResponseEntity<Void> sendCode(SendCodeRequest request);

    @Operation(summary = "인증코드 검증", description = "이메일과 전송된 인증코드를 검증합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "검증 성공"),
            @ApiResponse(responseCode = "400", description = "코드 불일치/만료")})
    ResponseEntity<Void> verify(VerifyCodeRequest request);
}
