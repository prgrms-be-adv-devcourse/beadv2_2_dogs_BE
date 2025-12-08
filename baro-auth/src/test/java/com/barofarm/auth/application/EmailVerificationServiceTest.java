package com.barofarm.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barofarm.auth.application.port.out.EmailCodeSender;
import com.barofarm.auth.domain.verification.EmailVerification;
import com.barofarm.auth.infrastructure.jpa.EmailVerificationJpaRepository;
import com.barofarm.auth.presentation.exception.BusinessException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationJpaRepository repository;

    @Mock
    private EmailCodeSender emailCodeSender;

    private Clock clock;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        clock = Clock.system(ZoneId.systemDefault());
        ReflectionTestUtils.setField(emailVerificationService, "clock", clock);
    }

    @Test
    @DisplayName("코드 전송 시 레코드가 저장되고 메일 발송이 호출된다")
    void sendVerification_saves_record_and_sends_email() {
        ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
        when(repository.save(any(EmailVerification.class))).thenAnswer(inv -> inv.getArgument(0));

        emailVerificationService.sendVerification("user@example.com");

        verify(repository).save(captor.capture());
        verify(emailCodeSender).send("user@example.com", captor.getValue().getCode());

        EmailVerification saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(saved.isVerified()).isFalse();
        assertThat(saved.getCode()).hasSize(6);
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now(clock));
    }

    @Test
    @DisplayName("정상 코드 검증 시 verified=true 로 변경된다")
    void verifyCode_marks_verified() {
        EmailVerification verification = EmailVerification.createNew("user@example.com", "123456",
                java.time.Duration.ofMinutes(5));
        when(repository.findByEmailAndCodeAndVerifiedIsFalse("user@example.com", "123456"))
                .thenReturn(Optional.of(verification));

        emailVerificationService.verifyCode("user@example.com", "123456");

        assertThat(verification.isVerified()).isTrue();
    }

    @Test
    @DisplayName("만료된 코드면 400 BAD_REQUEST 예외가 발생한다")
    void verifyCode_expired_throws() {
        EmailVerification verification = EmailVerification.createNew("expired@example.com", "999999",
                java.time.Duration.ofMinutes(5));
        // 강제로 만료 상태로 설정
        ReflectionTestUtils.setField(verification, "expiresAt", LocalDateTime.now(clock).minusMinutes(1));
        when(repository.findByEmailAndCodeAndVerifiedIsFalse("expired@example.com", "999999"))
                .thenReturn(Optional.of(verification));

        assertThatThrownBy(() -> emailVerificationService.verifyCode("expired@example.com", "999999"))
                .isInstanceOf(BusinessException.class).extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("ensureVerified: 최신 기록이 verified=false 이면 UNAUTHORIZED 예외")
    void ensureVerified_not_verified_throws() {
        EmailVerification latest = EmailVerification.createNew("user@example.com", "000000",
                java.time.Duration.ofMinutes(5));
        when(repository.findTopByEmailOrderByCreatedAtDesc("user@example.com")).thenReturn(Optional.of(latest));

        assertThatThrownBy(() -> emailVerificationService.ensureVerified("user@example.com"))
                .isInstanceOf(BusinessException.class).extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("ensureVerified: verified=true 이면 삭제가 호출된다")
    void ensureVerified_verified_deletes_record() {
        EmailVerification latest = EmailVerification.createNew("user@example.com", "000000",
                java.time.Duration.ofMinutes(5));
        latest.markVerified();
        when(repository.findTopByEmailOrderByCreatedAtDesc("user@example.com")).thenReturn(Optional.of(latest));

        emailVerificationService.ensureVerified("user@example.com");

        verify(repository).delete(latest);
    }
}
