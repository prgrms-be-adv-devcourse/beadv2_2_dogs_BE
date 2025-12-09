package com.barofarm.auth.application;

import com.barofarm.auth.application.port.out.EmailCodeSender;
import com.barofarm.auth.common.exception.CustomException;
import com.barofarm.auth.domain.verification.EmailVerification;
import com.barofarm.auth.exception.VerificationErrorCode;
import com.barofarm.auth.infrastructure.jpa.EmailVerificationJpaRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
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
                .orElseThrow(() -> new CustomException(VerificationErrorCode.CODE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now(clock);
        if (verification.isExpired(now)) {
            throw new CustomException(VerificationErrorCode.CODE_EXPIRED);
        }

        verification.markVerified();
    }

    /** 최종 회원가입에 노출 -> 인증 완료 확인 + 잔여 코드 정리 -> AuthService에서 사용 */
    public void ensureVerified(String email) {
        EmailVerification latest = repository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new CustomException(VerificationErrorCode.VERIFICATION_NOT_FOUND));

        if (!latest.isVerified()) {
            repository.deleteAllByEmail(email); // 진행중인 기록 정리
            throw new CustomException(VerificationErrorCode.VERIFICATION_NOT_COMPLETED);
        }

        repository.delete(latest); // 인증 완료 후 사용한 기록 정리
    }

    private String generateCode() {
        Random random = new Random();
        int num = random.nextInt(900_000) + 100_000; // 6자리 난수
        return String.valueOf(num);
    }
}
