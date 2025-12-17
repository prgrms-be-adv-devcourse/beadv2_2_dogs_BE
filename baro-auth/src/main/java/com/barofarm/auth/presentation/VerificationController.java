package com.barofarm.auth.presentation;

import com.barofarm.auth.application.EmailVerificationService;
import com.barofarm.auth.presentation.api.VerificationSwaggerApi;
import com.barofarm.auth.presentation.dto.verification.SendCodeRequest;
import com.barofarm.auth.presentation.dto.verification.VerifyCodeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 코드 발송 및 검증하기(이메일 코드 발송, 인증 코드)
@RestController
@RequestMapping("/api/v1/auth/verification")
@RequiredArgsConstructor
public class VerificationController implements VerificationSwaggerApi {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/email/send-code")
    public ResponseEntity<Void> sendCode(@RequestBody SendCodeRequest request) {
        emailVerificationService.sendVerification(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/verify-code")
    public ResponseEntity<Void> verify(@RequestBody VerifyCodeRequest request) {
        emailVerificationService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok().build();
    }
}
