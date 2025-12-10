package com.barofarm.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barofarm.auth.application.usecase.LoginCommand;
import com.barofarm.auth.application.usecase.SignUpCommand;
import com.barofarm.auth.application.usecase.TokenResult;
import com.barofarm.auth.common.exception.CustomException;
import com.barofarm.auth.domain.credential.AuthCredential;
import com.barofarm.auth.domain.token.RefreshToken;
import com.barofarm.auth.domain.user.User;
import com.barofarm.auth.exception.AuthErrorCode;
import com.barofarm.auth.infrastructure.jpa.AuthCredentialJpaRepository;
import com.barofarm.auth.infrastructure.jpa.RefreshTokenJpaRepository;
import com.barofarm.auth.infrastructure.jpa.UserJpaRepository;
import com.barofarm.auth.infrastructure.security.JwtTokenProvider;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private AuthCredentialJpaRepository credentialRepository;

    @Mock
    private RefreshTokenJpaRepository refreshTokenRepository;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private Clock clock;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"));
        // 새 clock을 주입하기 위해 리플렉션 사용
        ReflectionTestUtils.setField(authService, "clock", clock);
    }

    @Test
    @DisplayName("회원가입: 인증 완료 + 중복 아님이면 User/Credential/RefreshToken 생성")
    void signUpSuccessCreatesUserAndRefresh() {
        SignUpCommand cmd = new SignUpCommand("user@example.com", "raw", "Jane", "010", true);

        when(credentialRepository.existsByLoginEmail(cmd.email())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("access");
        when(jwtTokenProvider.generateRefreshToken(any(), any(), any())).thenReturn("refresh");
        when(jwtTokenProvider.getRefreshTokenValidity()).thenReturn(Duration.ofDays(14));

        // 저장 시 id 주입
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
            return u;
        });
        when(credentialRepository.save(any(AuthCredential.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = authService.signUp(cmd);

        assertThat(result.email()).isEqualTo(cmd.email());
        verify(emailVerificationService).ensureVerified(cmd.email());
        verify(refreshTokenRepository).deleteAllByUserId(any(UUID.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("회원가입: 이메일 중복이면 CONFLICT 예외")
    void signUpDuplicateEmailThrows() {
        SignUpCommand cmd = new SignUpCommand("dup@example.com", "raw", "Jane", "010", true);
        when(credentialRepository.existsByLoginEmail(cmd.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(cmd)).isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(AuthErrorCode.EMAIL_ALREADY_EXISTS);

        verify(emailVerificationService).ensureVerified(cmd.email());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그인: 비밀번호 불일치 시 UNAUTHORIZED 예외")
    void loginPasswordMismatchThrows() {
        LoginCommand cmd = new LoginCommand("user@example.com", "wrong");
        AuthCredential stored = AuthCredential.create(UUID.randomUUID(), cmd.email(), "hash", "salt");
        when(credentialRepository.findByLoginEmail(cmd.email())).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches(eq(cmd.password() + stored.getSalt()), eq(stored.getPasswordHash())))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(cmd)).isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(AuthErrorCode.INVALID_CREDENTIAL);
    }

    @Test
    @DisplayName("리프레시: 정상 토큰이면 회전 후 새 액세스/리프레시 토큰 반환")
    void refreshRotatesTokens() {
        String refresh = "old-refresh";
        UUID userId = UUID.randomUUID();
        RefreshToken stored = RefreshToken.issue(userId, refresh, Duration.ofDays(14));
        when(refreshTokenRepository.findByToken(refresh)).thenReturn(Optional.of(stored));
        when(jwtTokenProvider.validateToken(refresh)).thenReturn(true);
        when(jwtTokenProvider.getEmail(refresh)).thenReturn("user@example.com");
        User user = User.create("user@example.com", "user", "010", false);
        ReflectionTestUtils.setField(user, "id", userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(userId, "user@example.com", "CUSTOMER")).thenReturn("new-access");
        when(jwtTokenProvider.generateRefreshToken(userId, "user@example.com", "CUSTOMER")).thenReturn("new-refresh");
        when(jwtTokenProvider.getRefreshTokenValidity()).thenReturn(Duration.ofDays(14));

        TokenResult result = authService.refresh(refresh);

        assertThat(result.accessToken()).isEqualTo("new-access");
        assertThat(result.refreshToken()).isEqualTo("new-refresh");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(1)).save(captor.capture());
        RefreshToken rotated = captor.getValue();
        assertThat(rotated.getToken()).isEqualTo("new-refresh");
        assertThat(rotated.isRevoked()).isFalse();
        assertThat(rotated.getExpiresAt()).isEqualTo(LocalDateTime.now(clock).plusDays(14));
    }

    @Test
    @DisplayName("리프레시: 만료/폐기/검증 실패 시 UNAUTHORIZED 예외")
    void refreshInvalidOrExpiredTokenThrows() {
        String token = "bad-refresh";
        RefreshToken revoked = RefreshToken.issueWithExpiry(UUID.randomUUID(), token,
                LocalDateTime.now(clock).minusMinutes(1)); // 만료
        revoked.revoke();
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> authService.refresh(token)).isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(AuthErrorCode.REFRESH_TOKEN_UNUSABLE);
    }
}
