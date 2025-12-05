package com.barofarm.auth.api;

import com.barofarm.auth.application.EmailVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 코드 발송 및 검증하는 곳 (이메일 코드 혹은 휴대폰 인증 코드)
@RestController
@RequestMapping("/auth/verification")
public class VerificationController {

    private final EmailVerificationService emailVerificationService;

    public VerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/email/send-code")
    public ResponseEntity<Void> sendCode(@RequestBody SendCodeRequest request) {
        emailVerificationService.sendVerification(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Void> verify(@RequestBody VerifyCodeRequest request) {
        emailVerificationService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok().build();
    }

    // TODO: 일단 대략 필요할 거 같은 DTO 만들어서 여기다 두고 분리

    public record SendCodeRequest(String email) {
    }

    public record VerifyCodeRequest(String email, String code) {
    }

}
