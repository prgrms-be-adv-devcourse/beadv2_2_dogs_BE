package com.barofarm.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.barofarm.auth.api.exception.BusinessException;
import com.barofarm.auth.application.port.out.EmailCodeSender;
import com.barofarm.auth.domain.verification.EmailVerification;
import com.barofarm.auth.infrastructure.jpa.EmailVerificationJpaRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EmailVerificationServiceTest {

  @Mock EmailVerificationJpaRepository repository;

  @Mock EmailCodeSender emailCodeSender;

  // 시간은 고정해놓기
  @Mock Clock clock;

  @InjectMocks EmailVerificationService emailVerificationService;

  @BeforeEach
  void setUp() {
    lenient().when(clock.instant()).thenReturn(Instant.parse("2025-01-01T00:00:00.00Z"));
    lenient().when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));
  }

  @Test
  void sendVerificationCode_saveEntity_and_sendsMail() {
    // given
    String email = "test@example.com";

    // when
    emailVerificationService.sendVerification(email);

    // then
    ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);

    verify(repository).save(captor.capture());
    verify(emailCodeSender).send(eq(email), anyString());

    EmailVerification saved = captor.getValue();
    assertThat(saved.getEmail()).isEqualTo(email);
    assertThat(saved.isVerified()).isFalse();
    assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.of(2025, 1, 1, 0, 0));
  }

  @Test
  void verifyCode_throws_if_not_found() {
    // given
    when(repository.findByEmailAndCodeAndVerifiedIsFalse(anyString(), anyString()))
        .thenReturn(Optional.empty());

    // then
    assertThatThrownBy(() -> emailVerificationService.verifyCode("test@example.com", "123456"))
        .isExactlyInstanceOf(BusinessException.class);
  }
}
