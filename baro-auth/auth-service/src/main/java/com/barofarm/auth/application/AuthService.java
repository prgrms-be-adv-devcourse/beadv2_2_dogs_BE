package com.barofarm.auth.application;

import com.barofarm.auth.api.exception.BusinessException;
import com.barofarm.auth.domain.credential.AuthCredential;
import com.barofarm.auth.domain.user.User;
import com.barofarm.auth.infrastructure.jpa.AuthCredentialJpaRepository;
import com.barofarm.auth.infrastructure.jpa.UserJpaRepository;
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

  public AuthService(
      UserJpaRepository userRepository,
      AuthCredentialJpaRepository credentialRepository,
      EmailVerificationService emailVerificationService,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.credentialRepository = credentialRepository;
    this.emailVerificationService = emailVerificationService;
    this.passwordEncoder = passwordEncoder;
  }

  // 일단 DTO 여기다가 넣어놓기
  // 회원가입 요청 DTO

  public record SignUpRequest(
      String email, String password, String name, String phone, boolean marketingConsent) {}

  // 회원가입 응답 DTO
  // TODO : JWT 추가 예정
  public record SignUpResponse(Long userId, String email) {}

  public SignUpResponse signUp(SignUpRequest request) {

    // 1. 이메일 인증 여부 확인
    emailVerificationService.ensureVerified(request.email);

    // 2. 이메일 중복 체크하기 -> USER랑 AuthCredential 두개에 다 email이 있는데 어딜 기준으로 해야 하지. -> Auth
    if (credentialRepository.existsByLoginEmail(request.email)) {
      throw new BusinessException(HttpStatus.CONFLICT, "해당 이메일이 이미 존재합니다.");
    }

    // 3. User Entity 생성 및 저장
    User user =
        User.create(request.email(), request.name(), request.phone(), request.marketingConsent());
    userRepository.save(user);

    // 4. AuthCre. 엔티티 생성 및 저장
    String encodedPassword = passwordEncoder.encode(request.password());
    AuthCredential credential =
        AuthCredential.create(user.getId(), request.email(), encodedPassword);
    credentialRepository.save(credential);

    return new SignUpResponse(user.getId(), request.email());
  }
}
