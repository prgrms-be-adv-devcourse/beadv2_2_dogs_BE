package com.barofarm.auth.application;

import com.barofarm.auth.application.port.out.EmailCodeSender;
import com.barofarm.auth.domain.verification.EmailVerification;
import com.barofarm.auth.infrastructure.jpa.EmailVerificationJpaRepository;
import com.barofarm.auth.presentation.exception.BusinessException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmailVerificationService {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    private final EmailVerificationJpaRepository repository;
    private final EmailCodeSender emailCodeSender;
    private final Clock clock;

    @Autowired
    public EmailVerificationService(EmailVerificationJpaRepository repository, EmailCodeSender emailCodeSender,
            Clock clock) {
        this.repository = repository;
        this.emailCodeSender = emailCodeSender;
        this.clock = clock;
    }

    public void sendVerification(String email) {
        String code = generateCode();
        EmailVerification verification = EmailVerification.createNew(email, code, DEFAULT_TTL);
        repository.save(verification);

        emailCodeSender.send(email, code);
    }

    public void verifyCode(String email, String code) {
        EmailVerification verification = repository.findByEmailAndCodeAndVerifiedIsFalse(email, code)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "이메일 인증 코드를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now(clock);
        if (verification.isExpired(now)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다.");
        }

        verification.markVerified();
    }

    /** 최종 회원가입에 노출 -> 인증 완료 확인 + 잔여 코드 정리 -> AuthService에서 사용 */
    public void ensureVerified(String email) {
        EmailVerification latest = repository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "이메일 인증 이력이 없습니다."));

        if (!latest.isVerified()) {
            repository.deleteAllByEmail(email); // 진행중인 기록 정리
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "이메일 인증이 완료되지 않았습니다.");
        }

        repository.delete(latest); // 인증 완료 후 사용한 기록 정리
    }

    private String generateCode() {
        Random random = new Random();
        int num = random.nextInt(900_000) + 100_000; // 6자리 난수
        return String.valueOf(num);
    }
}
