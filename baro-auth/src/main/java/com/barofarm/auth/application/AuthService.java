package com.barofarm.auth.application;

import com.barofarm.auth.application.usecase.LoginCommand;
import com.barofarm.auth.application.usecase.LoginResult;
import com.barofarm.auth.application.usecase.PasswordChangeCommand;
import com.barofarm.auth.application.usecase.PasswordResetCommand;
import com.barofarm.auth.application.usecase.SignUpCommand;
import com.barofarm.auth.application.usecase.SignUpResult;
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
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserJpaRepository userRepository;
    private final AuthCredentialJpaRepository credentialRepository;
    private final RefreshTokenJpaRepository refreshTokenRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final Clock clock;

    public SignUpResult signUp(SignUpCommand request) {
        emailVerificationService.ensureVerified(request.email());

        if (credentialRepository.existsByLoginEmail(request.email())) {
            throw new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.create(request.email(), request.name(), request.phone(), request.marketingConsent());
        userRepository.save(user);

        String salt = generateSalt();
        String encodedPassword = passwordEncoder.encode(request.password() + salt);
        AuthCredential credential = AuthCredential.create(user.getId(), request.email(), encodedPassword, salt);
        credentialRepository.save(credential);

        String role = resolveRole(user.getUserType());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail(), role);

        refreshTokenRepository.deleteAllByUserId(user.getId());
        refreshTokenRepository.save(
                RefreshToken.issue(user.getId(), refreshToken, jwtTokenProvider.getRefreshTokenValidity()));

        return new SignUpResult(user.getId(), request.email(), accessToken, refreshToken);
    }

    public LoginResult login(LoginCommand loginCommand) {
        AuthCredential credential = credentialRepository.findByLoginEmail(loginCommand.email())
                .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_CREDENTIAL));

        if (!passwordEncoder.matches(loginCommand.password() + credential.getSalt(), credential.getPasswordHash())) {
            throw new CustomException(AuthErrorCode.INVALID_CREDENTIAL);
        }

        UUID userId = credential.getUserId();
        String email = credential.getLoginEmail();

        User user = userRepository.findById(userId).orElseThrow(
            () -> new CustomException(AuthErrorCode.USER_NOT_FOUND)
        );

        String role = resolveRole(user.getUserType());

        String accessToken = jwtTokenProvider.generateAccessToken(userId, email, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId, email, role);

        refreshTokenRepository.deleteAllByUserId(userId);
        refreshTokenRepository.save(
                RefreshToken.issue(userId, refreshToken, jwtTokenProvider.getRefreshTokenValidity()));

        return new LoginResult(userId, email, accessToken, refreshToken);
    }

    public TokenResult refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        if (stored.isRevoked() || stored.isExpired(LocalDateTime.now(clock))) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_UNUSABLE);
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_TAMPERED);
        }

        UUID userId = stored.getUserId();
        String email = jwtTokenProvider.getEmail(refreshToken);

        User user = userRepository.findById(userId).orElseThrow(
            () -> new CustomException(AuthErrorCode.USER_NOT_FOUND)
        );
        String role = resolveRole(user.getUserType());

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, email, role);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, email, role);
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
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
        emailVerificationService.sendVerification(email);
    }

    public void resetPassword(PasswordResetCommand command) {
        // 코드 검증 및 기록 정리
        emailVerificationService.verifyCode(command.email(), command.code());
        emailVerificationService.ensureVerified(command.email());

        AuthCredential credential = credentialRepository.findByLoginEmail(command.email())
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        String salt = generateSalt();
        String newHash = passwordEncoder.encode(command.newPassword() + salt);
        credential.changePassword(newHash, salt);
        credentialRepository.save(credential);
        refreshTokenRepository.deleteAllByUserId(credential.getUserId()); // 기존 세션 폐기
    }

    public void changePassword(UUID userId, PasswordChangeCommand command) {
        AuthCredential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.CREDENTIAL_NOT_FOUND));

        if (!passwordEncoder.matches(command.currentPassword() + credential.getSalt(),
                credential.getPasswordHash())) {
            throw new CustomException(AuthErrorCode.CURRENT_PASSWORD_MISMATCH);
        }

        String salt = generateSalt();
        String newHash = passwordEncoder.encode(command.newPassword() + salt);
        credential.changePassword(newHash, salt);
        credentialRepository.save(credential);
        refreshTokenRepository.deleteAllByUserId(credential.getUserId()); // 기존 세션 폐기
    }

    // Seller로 enum 업데이트 하는 것과 관련한 부분
    public void grantSeller(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        if (user.getUserType() == User.UserType.SELLER) {
            return;
        }

        user.changeToSeller();

    }

    private String generateSalt() {
        byte[] bytes = new byte[32]; // 32 bytes -> 64 hex chars
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String resolveRole(User.UserType userType) {
        return switch (userType) {
            case CUSTOMER -> "CUSTOMER";
            case SELLER -> "SELLER";
            case ADMIN -> "ADMIN";
        };
    }
}
