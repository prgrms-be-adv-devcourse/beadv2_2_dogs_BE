package com.barofarm.auth.application;

import com.barofarm.auth.application.usecase.LoginCommand;
import com.barofarm.auth.application.usecase.LoginResult;
import com.barofarm.auth.application.usecase.PasswordChangeCommand;
import com.barofarm.auth.application.usecase.PasswordResetCommand;
import com.barofarm.auth.application.usecase.SignUpCommand;
import com.barofarm.auth.application.usecase.SignUpResult;
import com.barofarm.auth.application.usecase.TokenResult;
import com.barofarm.auth.domain.credential.AuthCredential;
import com.barofarm.auth.domain.token.RefreshToken;
import com.barofarm.auth.domain.user.User;
import com.barofarm.auth.infrastructure.jpa.AuthCredentialJpaRepository;
import com.barofarm.auth.infrastructure.jpa.RefreshTokenJpaRepository;
import com.barofarm.auth.infrastructure.jpa.UserJpaRepository;
import com.barofarm.auth.infrastructure.security.JwtTokenProvider;
import com.barofarm.auth.presentation.exception.BusinessException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserJpaRepository userRepository;
    private final AuthCredentialJpaRepository credentialRepository;
    private final RefreshTokenJpaRepository refreshTokenRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final Clock clock;

    public AuthService(UserJpaRepository userRepository, AuthCredentialJpaRepository credentialRepository,
            RefreshTokenJpaRepository refreshTokenRepository, EmailVerificationService emailVerificationService,
            PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, Clock clock) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailVerificationService = emailVerificationService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.clock = clock;
    }

    public SignUpResult signUp(SignUpCommand request) {
        emailVerificationService.ensureVerified(request.email());

        if (credentialRepository.existsByLoginEmail(request.email())) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
        }

        User user = User.create(request.email(), request.name(), request.phone(), request.marketingConsent());
        userRepository.save(user);

        String salt = generateSalt();
        String encodedPassword = passwordEncoder.encode(request.password() + salt);
        AuthCredential credential = AuthCredential.create(user.getId(), request.email(), encodedPassword, salt);
        credentialRepository.save(credential);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), "USER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail(), "USER");

        refreshTokenRepository.deleteAllByUserId(user.getId());
        refreshTokenRepository.save(
                RefreshToken.issue(user.getId(), refreshToken, jwtTokenProvider.getRefreshTokenValidity()));

        return new SignUpResult(user.getId(), request.email(), accessToken, refreshToken);
    }

    public LoginResult login(LoginCommand loginCommand) {
        AuthCredential credential = credentialRepository.findByLoginEmail(loginCommand.email())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(loginCommand.password() + credential.getSalt(), credential.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        UUID userId = credential.getUserId();
        String email = credential.getLoginEmail();

        String accessToken = jwtTokenProvider.generateAccessToken(userId, email, "USER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId, email, "USER");

        refreshTokenRepository.deleteAllByUserId(userId);
        refreshTokenRepository.save(
                RefreshToken.issue(userId, refreshToken, jwtTokenProvider.getRefreshTokenValidity()));

        return new LoginResult(userId, email, accessToken, refreshToken);
    }

    public TokenResult refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."));

        if (stored.isRevoked() || stored.isExpired(LocalDateTime.now(clock))) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "사용할 수 없는 리프레시 토큰입니다.");
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료됐거나 위조되었습니다.");
        }

        UUID userId = stored.getUserId();
        String email = jwtTokenProvider.getEmail(refreshToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, email, "USER");
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, email, "USER");
        Duration refreshValidity = jwtTokenProvider.getRefreshTokenValidity();
        LocalDateTime newRefreshExpiry = LocalDateTime.now(clock).plus(refreshValidity);
        stored.rotate(newRefreshToken, newRefreshExpiry);
        refreshTokenRepository.save(stored);

        return new TokenResult(userId, email, newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }

    public void requestPasswordReset(String email) {
        // 존재하는 계정만 대상
        credentialRepository.findByLoginEmail(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "가입된 이메일이 없습니다."));
        emailVerificationService.sendVerification(email);
    }

    public void resetPassword(PasswordResetCommand command) {
        // 코드 검증 및 기록 정리
        emailVerificationService.verifyCode(command.email(), command.code());
        emailVerificationService.ensureVerified(command.email());

        AuthCredential credential = credentialRepository.findByLoginEmail(command.email())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "가입된 이메일이 없습니다."));

        String salt = generateSalt();
        String newHash = passwordEncoder.encode(command.newPassword() + salt);
        credential.changePassword(newHash, salt);
        credentialRepository.save(credential);
        refreshTokenRepository.deleteAllByUserId(credential.getUserId()); // 기존 세션 폐기
    }

    public void changePassword(UUID userId, PasswordChangeCommand command) {
        AuthCredential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "자격 증명을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(command.currentPassword() + credential.getSalt(),
                credential.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 올바르지 않습니다.");
        }

        String salt = generateSalt();
        String newHash = passwordEncoder.encode(command.newPassword() + salt);
        credential.changePassword(newHash, salt);
        credentialRepository.save(credential);
        refreshTokenRepository.deleteAllByUserId(credential.getUserId()); // 기존 세션 폐기
    }

    private String generateSalt() {
        byte[] bytes = new byte[32]; // 32 bytes -> 64 hex chars
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
