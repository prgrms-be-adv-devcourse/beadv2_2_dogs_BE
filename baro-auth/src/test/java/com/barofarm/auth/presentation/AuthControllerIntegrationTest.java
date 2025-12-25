package com.barofarm.auth.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.barofarm.auth.domain.credential.AuthCredential;
import com.barofarm.auth.domain.token.RefreshToken;
import com.barofarm.auth.domain.user.User;
import com.barofarm.auth.domain.verification.EmailVerification;
import com.barofarm.auth.infrastructure.jpa.AuthCredentialJpaRepository;
import com.barofarm.auth.infrastructure.jpa.EmailVerificationJpaRepository;
import com.barofarm.auth.infrastructure.jpa.RefreshTokenJpaRepository;
import com.barofarm.auth.infrastructure.jpa.UserJpaRepository;
import com.barofarm.auth.infrastructure.security.JwtTokenProvider;
import com.barofarm.auth.presentation.dto.login.LoginRequest;
import com.barofarm.auth.presentation.dto.signup.SignupRequest;
import com.barofarm.auth.presentation.dto.verification.VerifyCodeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailVerificationJpaRepository emailVerificationJpaRepository;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private AuthCredentialJpaRepository credentialRepository;

    @Autowired
    private RefreshTokenJpaRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void clean() {
        refreshTokenRepository.deleteAll();
        emailVerificationJpaRepository.deleteAll();
        credentialRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("회원가입/로그인")
    class SignupLogin {

        @Test
        @DisplayName("이메일 인증을 마친 사용자는 회원가입에 성공하고 User/AuthCredential/RefreshToken이 생성된다")
        void signupWithVerifiedEmailCreatesUserTokensAndStoresRefresh() throws Exception {
            // given: 이메일 인증 완료 상태 준비
            String email = "user@example.com";
            String password = "P@ssw0rd!";
            createVerifiedEmail(email);
            SignupRequest payload = new SignupRequest(email, password, "Jane Doe", "010-1234-5678", true);

            // when & then
            mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload))).andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value(email)).andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());

            // then: DB 상태 검증
            assertThat(userRepository.count()).isEqualTo(1);
            assertThat(credentialRepository.count()).isEqualTo(1);
            assertThat(refreshTokenRepository.count()).isEqualTo(1);

            User savedUser = userRepository.findAll().get(0);
            AuthCredential savedCredential = credentialRepository.findAll().get(0);
            RefreshToken savedRefresh = refreshTokenRepository.findAll().get(0);

            assertThat(savedUser.getEmail()).isEqualTo(email);
            assertThat(savedUser.isMarketingConsent()).isTrue();
            assertThat(passwordEncoder.matches(password + savedCredential.getSalt(), savedCredential.getPasswordHash()))
                    .isTrue();
            assertThat(savedRefresh.getUserId()).isEqualTo(savedUser.getId());
            assertThat(savedRefresh.isRevoked()).isFalse();
            // 인증 기록은 삭제되어야 한다
            assertThat(emailVerificationJpaRepository.findTopByEmailOrderByCreatedAtDesc(email)).isEmpty();
        }

        @Test
        @DisplayName("이메일 인증 없이 회원가입하면 404를 반환한다")
        void signupWithoutVerificationFails() throws Exception {
            SignupRequest payload = new SignupRequest("novalid@example.com", "Passw0rd!", "No Verify", "010-0000-0000",
                    false);

            mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload))).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("올바른 자격 증명으로 로그인 시 Access/Refresh 토큰 발급 및 저장")
        void loginWithValidCredentialsReturnsTokensAndStoresRefresh() throws Exception {
            // given: 사용자/자격증명 미리 생성
            String email = "login@example.com";
            String rawPassword = "Secr3t!";
            User user = userRepository.save(User.create(email, "Login User", "010-0000-0000", false));
            saveCredential(user.getId(), email, rawPassword);

            LoginRequest payload = new LoginRequest(email, rawPassword);

            // when & then
            mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(user.getId().toString()))
                    .andExpect(jsonPath("$.email").value(email)).andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());

            assertThat(refreshTokenRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("비밀번호 불일치 시 401을 반환한다")
        void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
            // given
            String email = "login-fail@example.com";
            User user = userRepository.save(User.create(email, "Login Fail", "010-9999-9999", false));
            saveCredential(user.getId(), email, "Correct1!");

            LoginRequest payload = new LoginRequest(email, "WrongPass1!");

            // when & then
            mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload))).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("프로필 조회(/auth/me)")
    class MeEndpoint {

        @Test
        @DisplayName("유효한 Access 토큰으로 /auth/me 호출 시 현재 사용자 정보 반환")
        void meReturnsCurrentAuthenticatedUser() throws Exception {
            // given
            String email = "me@example.com";
            User user = userRepository.save(User.create(email, "Me User", "010-7777-7777", false));
            saveCredential(user.getId(), email, "MePass1!");
            String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), email, "USER");

            // when & then
            mockMvc.perform(get("/auth/me").header("Authorization", bearer(accessToken)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(user.getId().toString()))
                    .andExpect(jsonPath("$.email").value(email)).andExpect(jsonPath("$.role").value("USER"));
        }
    }

    @Nested
    @DisplayName("리프레시 토큰 회전/폐기")
    class RefreshAndLogout {

        @Test
        @DisplayName("정상 리프레시 토큰으로 재발급 시 기존 토큰을 교체한다")
        void refreshRotatesTokensAndReplacesOld() throws Exception {
            String email = "rotate@example.com";
            String rawPassword = "Rotate1!";
            User user = userRepository.save(User.create(email, "Rotate User", "010-3333-3333", false));
            saveCredential(user.getId(), email, rawPassword);

            LoginRequest loginPayload = new LoginRequest(email, rawPassword);
            MvcResult loginResult = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginPayload))).andExpect(status().isOk()).andReturn();

            String oldRefreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                    .get("refreshToken").asText();

            String refreshRequest = """
                    {"refreshToken": "%s"}
                    """.formatted(oldRefreshToken);

            MvcResult refreshResult = mockMvc
                    .perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(refreshRequest))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty()).andReturn();

            String newRefreshToken = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                    .get("refreshToken").asText();

            assertThat(refreshTokenRepository.count()).isEqualTo(1);
            assertThat(refreshTokenRepository.findByToken(oldRefreshToken)).isEmpty();
            RefreshToken current = refreshTokenRepository.findByToken(newRefreshToken).orElseThrow();
            assertThat(current.isRevoked()).isFalse();
        }

        @Test
        @DisplayName("폐기되었거나 만료된 리프레시 토큰이면 401을 반환한다")
        void refreshWithRevokedOrExpiredTokenFails() throws Exception {
            String email = "expired@example.com";
            User user = userRepository.save(User.create(email, "Expired User", "010-4444-4444", false));
            String expiredToken = jwtTokenProvider.generateRefreshToken(user.getId(), email, "USER");
            RefreshToken expired = RefreshToken.issueWithExpiry(user.getId(), expiredToken,
                    LocalDateTime.now().minusMinutes(1));
            expired.revoke();
            refreshTokenRepository.save(expired);

            String refreshRequest = """
                    {"refreshToken": "%s"}
                    """.formatted(expiredToken);

            mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(refreshRequest))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("로그아웃 호출 시 리프레시 토큰이 폐기된다")
        void logoutRevokesRefreshToken() throws Exception {
            String email = "logout@example.com";
            User user = userRepository.save(User.create(email, "Logout User", "010-5555-5555", false));
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), email, "USER");
            refreshTokenRepository
                    .save(RefreshToken.issue(user.getId(), refreshToken, jwtTokenProvider.getRefreshTokenValidity()));

            String logoutRequest = """
                    {"refreshToken": "%s"}
                    """.formatted(refreshToken);

            mockMvc.perform(post("/auth/logout").contentType(MediaType.APPLICATION_JSON).content(logoutRequest))
                    .andExpect(status().isOk());

            RefreshToken saved = refreshTokenRepository.findByToken(refreshToken).orElseThrow();
            assertThat(saved.isRevoked()).isTrue();
        }
    }

    @Nested
    @DisplayName("이메일 인증 부정 케이스")
    class EmailVerificationNegative {

        @Test
        @DisplayName("잘못된 코드로 검증하면 400을 반환한다")
        void verifyWrongCodeReturnsBadRequest() throws Exception {
            emailVerificationJpaRepository.save(EmailVerification.createNew("wrong@example.com", "123456",
                    Duration.ofMinutes(5)));

            VerifyCodeRequest request = new VerifyCodeRequest("wrong@example.com", "000000");

            mockMvc.perform(post("/auth/verification/email/verify").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("만료된 코드로 검증하면 400을 반환한다")
        void verifyExpiredCodeReturnsBadRequest() throws Exception {
            EmailVerification expired = EmailVerification.createNew("expired@example.com", "111111",
                    Duration.ofMinutes(-1));
            emailVerificationJpaRepository.save(expired);

            VerifyCodeRequest request = new VerifyCodeRequest("expired@example.com", "111111");

            mockMvc.perform(post("/auth/verification/email/verify").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
        }
    }

    // ===== 테스트 유틸 =====
    private void createVerifiedEmail(String email) {
        EmailVerification verification = EmailVerification.createNew(email, "123456", Duration.ofMinutes(5));
        verification.markVerified();
        emailVerificationJpaRepository.save(verification);
    }

    private void saveCredential(UUID userId, String email, String rawPassword) {
        String salt = generateSalt();
        String hash = passwordEncoder.encode(rawPassword + salt);
        credentialRepository.save(AuthCredential.create(userId, email, hash, salt));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String generateSalt() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
