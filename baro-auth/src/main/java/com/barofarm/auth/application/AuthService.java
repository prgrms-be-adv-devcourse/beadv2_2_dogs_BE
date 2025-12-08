package com.barofarm.auth.application;

import com.barofarm.auth.application.usecase.LoginCommand;
import com.barofarm.auth.application.usecase.LoginResult;
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

        // 1. 이메일 인증 완료 확인
        emailVerificationService.ensureVerified(request.email());

        // 2. 이메일 중복 체크 -> USER와 AuthCredential 모두에서 email이 unique여야 함 -> Auth
        if (credentialRepository.existsByLoginEmail(request.email())) {
            throw new BusinessException(HttpStatus.CONFLICT, "해당 이메일이 이미 존재합니다.");
        }

        // 3. User Entity 생성 및 저장
        User user = User.create(request.email(), request.name(), request.phone(), request.marketingConsent());
        userRepository.save(user);

        // 4. AuthCredential 생성 및 저장 (salt 포함)
        String salt = generateSalt();
        String encodedPassword = passwordEncoder.encode(request.password() + salt);
        AuthCredential credential = AuthCredential.create(user.getId(), request.email(), encodedPassword, salt);
        credentialRepository.save(credential);

        // 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), "USER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail(), "USER");

        // 기존 리프레시 토큰 제거 후 새 토큰 저장
        refreshTokenRepository.deleteAllByUserId(user.getId());
        refreshTokenRepository.save(
                RefreshToken.issue(user.getId(), refreshToken, jwtTokenProvider.getRefreshTokenValidity()));

        return new SignUpResult(user.getId(), request.email(), accessToken, refreshToken);
    }

    public LoginResult login(LoginCommand loginCommand) {

        // 1. 이메일 존재 확인
        AuthCredential credential = credentialRepository.findByLoginEmail(loginCommand.email())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        // 2. 비밀번호 매칭 (salt 활용)
        if (!passwordEncoder.matches(loginCommand.password() + credential.getSalt(), credential.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        UUID userId = credential.getUserId();
        String email = credential.getLoginEmail();

        // 3. 토큰 발급
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
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었거나 위조되었습니다.");
        }

        UUID userId = stored.getUserId();
        String email = jwtTokenProvider.getEmail(refreshToken);

        // 회전: 기존 토큰 폐기 후 새 토큰 저장
        stored.revoke();
        refreshTokenRepository.save(stored);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, email, "USER");
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, email, "USER");
        refreshTokenRepository.save(
                RefreshToken.issue(userId, newRefreshToken, jwtTokenProvider.getRefreshTokenValidity()));

        return new TokenResult(userId, email, newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }

    private String generateSalt() {
        byte[] bytes = new byte[32]; // 32 bytes -> 64 hex chars
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
