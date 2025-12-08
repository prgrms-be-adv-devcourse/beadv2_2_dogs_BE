package com.barofarm.auth.api;

import com.barofarm.auth.api.dto.SendCodeRequest;
import com.barofarm.auth.api.dto.VerifyCodeRequest;
import com.barofarm.auth.application.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 코드 발송 및 검증하기(이메일 코드 발송, 인증 코드)
@RestController
@RequestMapping("auth/verification")
@Tag(name = "Email Verification", description = "이메일 인증코드 발송 및 확인")
public class VerificationController {

    private final EmailVerificationService emailVerificationService;

    public VerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/email/send-code")
    @Operation(summary = "인증코드 발송", description = "이메일로 인증코드를 전송합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "발송 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 이메일")})
    public ResponseEntity<Void> sendCode(@RequestBody SendCodeRequest request) {
        emailVerificationService.sendVerification(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/verify")
    @Operation(summary = "인증코드 검증", description = "이메일과 수신한 인증코드를 검증합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "검증 성공"),
            @ApiResponse(responseCode = "400", description = "인증코드 불일치/만료")})
    public ResponseEntity<Void> verify(@RequestBody VerifyCodeRequest request) {
        emailVerificationService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok().build();
    }
}
