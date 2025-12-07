package com.barofarm.auth.application;

import com.barofarm.auth.api.exception.BusinessException;
import com.barofarm.auth.application.usecase.LoginCommand;
import com.barofarm.auth.application.usecase.LoginResult;
import com.barofarm.auth.application.usecase.SignUpCommand;
import com.barofarm.auth.application.usecase.SignUpResult;
import com.barofarm.auth.domain.credential.AuthCredential;
import com.barofarm.auth.domain.user.User;
import com.barofarm.auth.infrastructure.jpa.AuthCredentialJpaRepository;
import com.barofarm.auth.infrastructure.jpa.UserJpaRepository;
import com.barofarm.auth.infrastructure.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

  private final UserJpaRepository userRepository;
  private final AuthCredentialJpaRepository credentialRepository;
  private final EmailVerificationService emailVerificationService;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  public AuthService(
      UserJpaRepository userRepository,
      AuthCredentialJpaRepository credentialRepository,
      EmailVerificationService emailVerificationService,
      PasswordEncoder passwordEncoder,
      JwtTokenProvider jwtTokenProvider) {
    this.userRepository = userRepository;
    this.credentialRepository = credentialRepository;
    this.emailVerificationService = emailVerificationService;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  public SignUpResult signUp(SignUpCommand request) {

    // 1. 이메일 인증 여부 확인
    emailVerificationService.ensureVerified(request.email());

    // 2. 이메일 중복 체크 -> USER와 AuthCredential 모두에서 email이 unique여야 함 -> Auth
    if (credentialRepository.existsByLoginEmail(request.email())) {
      throw new BusinessException(HttpStatus.CONFLICT, "해당 이메일이 이미 존재합니다.");
    }

    // 3. User Entity 생성 및 저장
    User user =
        User.create(request.email(), request.name(), request.phone(), request.marketingConsent());
    userRepository.save(user);

    // 4. AuthCredential 생성 및 저장
    String encodedPassword = passwordEncoder.encode(request.password());
    AuthCredential credential =
        AuthCredential.create(user.getId(), request.email(), encodedPassword);
    credentialRepository.save(credential);

    // [추가] 토큰 발급
    String accessToken =
        jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), "USER");
    String refreshToken =
        jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail(), "USER");

    return new SignUpResult(user.getId(), request.email(), accessToken, refreshToken);
  }

  public LoginResult login(LoginCommand loginCommand) {

    // 1. 이메일 존재 확인
    AuthCredential credential =
        credentialRepository
            .findByLoginEmail(loginCommand.email())
            .orElseThrow(
                () -> new BusinessException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

    // 2. 비밀번호 매칭
    if (!passwordEncoder.matches(loginCommand.password(), credential.getPasswordHash())) {
      throw new BusinessException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    Long userId = credential.getUserId();
    String email = credential.getLoginEmail();

    // 3. 토큰 발급
    String accessToken = jwtTokenProvider.generateAccessToken(userId, email, "USER");
    String refreshToken = jwtTokenProvider.generateRefreshToken(userId, email, "USER");

    return new LoginResult(userId, email, accessToken, refreshToken);
  }
}
