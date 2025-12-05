package com.barofarm.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.barofarm.auth.api.exception.BusinessException;
import com.barofarm.auth.domain.credential.AuthCredential;
import com.barofarm.auth.domain.user.User;
import com.barofarm.auth.infrastructure.jpa.AuthCredentialJpaRepository;
import com.barofarm.auth.infrastructure.jpa.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
public class AuthServiceTest {

  @Mock UserJpaRepository userRepository;

  @Mock AuthCredentialJpaRepository authCredentialJpaRepository;

  @Mock EmailVerificationService emailVerificationService;

  @Mock PasswordEncoder passwordEncoder;

  @InjectMocks AuthService authService;

  @Test
  @DisplayName("회원가입 성공: 인증 완료 시 자격 증명·유저 저장")
  void signUpRequestTest() {

    // given
    AuthService.SignUpRequest req =
        new AuthService.SignUpRequest("test@example.com", "password", "테스터", "010-1111-2222", true);

    // 이메일 인증이 끝난 상태로 설정
    doNothing().when(emailVerificationService).ensureVerified(req.email());
    when(authCredentialJpaRepository.existsByLoginEmail(req.email())).thenReturn(false);
    when(passwordEncoder.encode("password")).thenReturn("ENCODED_PW");

    // save 호출 시 전달받은 객체를 그대로 반환하도록 간단히 mocking
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User u = invocation.getArgument(0);
              return u;
            });
    // when
    AuthService.SignUpResponse res = authService.signUp(req);

    // then
    assertThat(res.email()).isEqualTo(req.email());
    verify(emailVerificationService).ensureVerified(req.email());
    verify(authCredentialJpaRepository).save(any(AuthCredential.class));
  }

  @Test
  @DisplayName("회원가입 실패: 이메일 인증 미완료 시 예외 발생")
  void signupFailedTest() {
    // given
    AuthService.SignUpRequest req =
        new AuthService.SignUpRequest("test@exam.com", "pw", "가나다", "010-1111-2222", true);

    doThrow(new BusinessException(HttpStatus.BAD_GATEWAY, "이메일이 인증되지 않았습니다."))
        .when(emailVerificationService)
        .ensureVerified(req.email());

    // when & then
    assertThatThrownBy(() -> authService.signUp(req)).isInstanceOf(BusinessException.class);

    verifyNoInteractions(userRepository);
    verifyNoInteractions(authCredentialJpaRepository);
  }

  @Test
  @DisplayName("회원가입 실패: 이미 존재하는 이메일로 요청하면 예외 발생")
  void signup_fails_when_email_already_exists() {
    // given
    AuthService.SignUpRequest req =
        new AuthService.SignUpRequest("test@example.com", "pw", "홍길동", "010-1111-2222", true);

    doNothing().when(emailVerificationService).ensureVerified(req.email());
    when(authCredentialJpaRepository.existsByLoginEmail(req.email())).thenReturn(true);

    // when & then
    assertThatThrownBy(() -> authService.signUp(req)).isInstanceOf(BusinessException.class);

    verifyNoInteractions(userRepository);
    verify(authCredentialJpaRepository, never()).save(any());
  }
}
