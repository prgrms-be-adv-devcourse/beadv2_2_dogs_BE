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

    // Spring이 사용하는 생성자
    @Autowired
    public EmailVerificationService(EmailVerificationJpaRepository repository, EmailCodeSender emailCodeSender,
            Clock clock) {
        this.repository = repository;
        this.emailCodeSender = emailCodeSender;
        this.clock = clock;
    }

    // TODO : 이것도 나중엔 외부로 연결
    // 테스트 위해서 만들어 놓은 것
    public void sendVerification(String email) {
        String code = generateCode();
        EmailVerification verification = EmailVerification.createNew(email, code, DEFAULT_TTL);
        repository.save(verification);

        emailCodeSender.send(email, code);
    }

    // 이메일과 인증 코드가 유효한지 확인하기
    public void verifyCode(String email, String code) {
        EmailVerification verification = repository.findByEmailAndCodeAndVerifiedIsFalse(email, code)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "이메일 인증 코드를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now(clock);
        if (verification.isExpired(now)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다.");
        }

        verification.markVerified();
        // @Transactional 이어서 flush 시점에 UPDATE 됨
    }

    /** 최종 회원가입 전에 호출 -> 인정 완료 여부 확인 + 레코드 제거 -> AuthService에서 사용 */
    public void ensureVerified(String email) {
        EmailVerification latest = repository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "이메일 인증 이력이 없습니다.")); // 완료되면 바로바로 삭제 해놔서

        if (!latest.isVerified()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "이메일 인증이 완료되지 않았습니다.");
        }

        repository.delete(latest);
    }

    // TODO: 진짜 외부 서비스를 이용한 구현은 향후
    private String generateCode() {
        Random random = new Random();
        // 6자리 랜덤 정수 만들기
        int num = random.nextInt(900_000) + 100_000;

        return String.valueOf(num);
    }
}
