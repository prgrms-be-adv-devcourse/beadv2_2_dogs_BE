package com.barofarm.auth.application;

import com.barofarm.auth.application.event.HotlistEventMessage;
import com.barofarm.auth.application.port.HotlistEventPublisher;
import com.barofarm.auth.application.port.out.OAuthAccountRepository;
import com.barofarm.auth.application.port.out.OAuthProviderClient;
import com.barofarm.auth.application.port.out.OAuthStateStore;
import com.barofarm.auth.application.usecase.LoginCommand;
import com.barofarm.auth.application.usecase.LoginResult;
import com.barofarm.auth.application.usecase.OAuthCallbackCommand;
import com.barofarm.auth.application.usecase.OAuthLinkCallbackCommand;
import com.barofarm.auth.application.usecase.OAuthLinkStartResult;
import com.barofarm.auth.application.usecase.OAuthLoginStateResult;
import com.barofarm.auth.application.usecase.PasswordChangeCommand;
import com.barofarm.auth.application.usecase.PasswordResetCommand;
import com.barofarm.auth.application.usecase.SignUpCommand;
import com.barofarm.auth.application.usecase.SignUpResult;
import com.barofarm.auth.application.usecase.TokenResult;
import com.barofarm.auth.application.usecase.WithdrawCommand;
import com.barofarm.auth.domain.credential.AuthCredential;
import com.barofarm.auth.domain.oauth.OAuthAccount;
import com.barofarm.auth.domain.oauth.OAuthUserInfo;
import com.barofarm.auth.domain.token.RefreshToken;
import com.barofarm.auth.domain.user.SellerStatus;
import com.barofarm.auth.domain.user.User;
import com.barofarm.auth.exception.AuthErrorCode;
import com.barofarm.auth.infrastructure.jpa.AuthCredentialJpaRepository;
import com.barofarm.auth.infrastructure.jpa.RefreshTokenJpaRepository;
import com.barofarm.auth.infrastructure.jpa.UserJpaRepository;
import com.barofarm.auth.infrastructure.outbox.OutboxEventService;
import com.barofarm.auth.infrastructure.security.JwtTokenProvider;
import com.barofarm.auth.presentation.dto.admin.AdminUserSummaryResponse;
import com.barofarm.exception.CustomException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final OAuthAccountRepository oauthAccountRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthProviderClient oauthProviderClient;
    private final OAuthStateStore oauthStateStore;
    private final HotlistEventPublisher hotlistEventPublisher;
    private final OutboxEventService outboxEventService;
    private final Clock clock;

    public SignUpResult signUp(SignUpCommand request) {
        emailVerificationService.ensureVerified(request.email());

        if (credentialRepository.existsByLoginEmail(request.email())) {
            throw new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.create(request.email(), request.name(), request.phone(), request.marketingConsent());
        user.touchLastLogin(now());
        userRepository.save(user);

        String salt = generateSalt();
        String encodedPassword = passwordEncoder.encode(request.password() + salt);
        AuthCredential credential = AuthCredential.create(user.getId(), request.email(), encodedPassword, salt);
        credentialRepository.save(credential);

        // [1] 토큰 발급 흐름을 issueTokens()로 통합해 중복을 제거한다.
        //     (로그인/회원가입/소셜 로그인 모두 같은 토큰 정책을 쓰도록 캡슐화)
        TokenResult tokens = issueTokens(user);
        return new SignUpResult(user.getId(), request.email(), tokens.accessToken(), tokens.refreshToken());
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
        user.touchLastLogin(now());
        userRepository.save(user);

        // [1] 회원가입과 동일한 토큰 정책을 재사용한다.
        TokenResult tokens = issueTokens(user);
        return new LoginResult(userId, email, tokens.accessToken(), tokens.refreshToken());
    }

    public TokenResult oauthCallback(OAuthCallbackCommand command) {
        if (!oauthStateStore.validateLoginState(command.state())) {
            throw new CustomException(AuthErrorCode.OAUTH_INVALID_STATE);
        }

        // 1) 공급자에서 사용자 정보를 조회한다.
        OAuthUserInfo userInfo = oauthProviderClient.fetchUserInfo(
            command.provider(), command.code(), command.state());

        // 2) 이미 연결된 소셜 계정인지 확인한다.
        OAuthAccount account = oauthAccountRepository
            .findByProviderAndProviderUserId(command.provider(), userInfo.providerUserId())
            .orElse(null);

        if (account == null) {
            // 3) 이메일 충돌 여부를 확인하고, 필요 시 연결을 요구한다.
            handleLinkRequired(userInfo);
            // 4) 신규 가입 + 소셜 계정 연결을 동시에 처리한다.
            User user = createOAuthUser(userInfo);
            account = OAuthAccount.link(user.getId(), userInfo);
        }

        // 5) 로그인 시각 갱신 후 JWT를 발급한다.
        LocalDateTime now = now();
        account.touchLastLogin(now);
        oauthAccountRepository.save(account);

        User user = userRepository.findById(account.getUserId())
            .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
        user.touchLastLogin(now);
        userRepository.save(user);

        return issueTokens(user);
    }

    public OAuthLoginStateResult startOAuthLogin() {
        // [2] 로그인용 state는 인증 전 단계에서 발급되므로 별도 API로 분리한다.
        String state = oauthStateStore.issueLoginState();
        return new OAuthLoginStateResult(state);
    }

    public OAuthLinkStartResult startOAuthLink(UUID userId) {
        // [3] 연결용 state는 로그인된 사용자에게만 발급한다.
        String state = oauthStateStore.issueLinkState(userId);
        return new OAuthLinkStartResult(state);
    }

    public void oauthLinkCallback(OAuthLinkCallbackCommand command) {
        UUID linkedUserId = oauthStateStore.consumeLinkState(command.state());
        if (linkedUserId == null || !linkedUserId.equals(command.userId())) {
            throw new CustomException(AuthErrorCode.OAUTH_INVALID_STATE);
        }

        // 1) 공급자에서 사용자 정보를 조회한다.
        OAuthUserInfo userInfo = oauthProviderClient.fetchUserInfo(
            command.provider(), command.code(), command.state());

        // 2) 이미 다른 사용자에게 연결된 계정인지 확인한다.
        OAuthAccount existing = oauthAccountRepository
            .findByProviderAndProviderUserId(command.provider(), userInfo.providerUserId())
            .orElse(null);

        if (existing != null && !existing.getUserId().equals(command.userId())) {
            throw new CustomException(AuthErrorCode.OAUTH_ALREADY_LINKED);
        }

        // 3) 본인 계정이면 연결을 생성하거나, 기존 연결을 재사용한다.
        OAuthAccount account = existing != null
            ? existing
            : OAuthAccount.link(command.userId(), userInfo);

        account.touchLastLogin(now());
        oauthAccountRepository.save(account);
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
        Map<String, Object> entitlements = buildEntitlements(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, email, role, entitlements);
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
        userRepository.save(user);
        // [1] 판매자 승인 상태를 hotlist에 반영해 토큰 갱신 전에도 SELLER 권한이 적용되도록 한다.
        publishSellerStatusEvent(user.getId(), SellerStatus.APPROVED, "SELLER_APPROVED");

    }

    // Admin-controlled account state updates that drive gateway/OPA decisions.
    public void updateUserState(UUID userId, User.UserState userState, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        user.changeState(userState);
        userRepository.save(user);

        if (userState != User.UserState.ACTIVE) {
            refreshTokenRepository.deleteAllByUserId(userId); // Force re-auth for blocked users.
        }

        publishUserStateEvent(user, userState, reason);
    }

    // Admin-controlled seller status updates (APPROVED/REJECTED/SUSPENDED).
    public void updateSellerStatus(UUID sellerId, SellerStatus status, String reason) {
        User user = userRepository.findById(sellerId)
            .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // [1] 승인 시에는 SELLER 권한 부여(토큰 갱신 없이도 hotlist로 권한 인정).
        if (status == SellerStatus.APPROVED && user.getUserType() != User.UserType.SELLER) {
            user.changeToSeller();
            userRepository.save(user);
        }

        // [2] OPA hotlist 반영을 위해 seller 상태 이벤트 발행.
        publishSellerStatusEvent(user.getId(), status, reason);
    }

    public void withdrawUser(UUID userId, WithdrawCommand command) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        boolean stateChanged = user.getUserState() != User.UserState.WITHDRAWN;
        if (stateChanged) {
            String anonymizedEmail = buildWithdrawnEmail(userId);
            String anonymizedName = "WITHDRAWN";
            user.withdraw(anonymizedEmail, anonymizedName);
            userRepository.save(user);
        }

        // [1] 탈퇴 시 자격 증명/세션을 즉시 폐기해 재사용을 차단한다.
        credentialRepository.deleteByUserId(userId);
        oauthAccountRepository.deleteAllByUserId(userId);
        refreshTokenRepository.deleteAllByUserId(userId);

        // [2] OPA 핫리스트 반영 + 아웃박스 적재는 상태 변경 시에만 수행한다.
        if (stateChanged) {
            String reason = normalizeReason(command);
            publishUserStateEvent(user, User.UserState.WITHDRAWN, reason);
            outboxEventService.enqueueUserWithdrawnEvent(user, reason);
        }
    }

    private String generateSalt() {
        byte[] bytes = new byte[32]; // 32 bytes -> 64 hex chars
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String buildWithdrawnEmail(UUID userId) {
        return "withdrawn+" + userId + "@deleted.local";
    }

    private String normalizeReason(WithdrawCommand command) {
        if (command == null || command.reason() == null || command.reason().isBlank()) {
            return "USER_WITHDRAW";
        }
        return command.reason().trim();
    }

    private String resolveRole(User.UserType userType) {
        return switch (userType) {
            case CUSTOMER -> "CUSTOMER";
            case SELLER -> "SELLER";
            case ADMIN -> "ADMIN";
        };
    }

    private Map<String, Object> buildEntitlements(User user) {
        Map<String, Object> entitlements = new HashMap<>();
        // Default entitlements for gateway/OPA. Hotlist data overrides stale tokens.
        // Flags are coarse-grained, endpoint-level blocks (review/reservation/order/publish).
        entitlements.put("user_status", user.getUserState().name());
        entitlements.put("seller_status", "UNKNOWN");
        entitlements.put("flags", List.of());
        entitlements.put("ver", 1);
        return entitlements;
    }

    private void publishUserStateEvent(User user, User.UserState userState, String reason) {
        HotlistEventMessage event = new HotlistEventMessage();
        event.setEventId(UUID.randomUUID().toString());
        event.setSubjectType("user");
        event.setSubjectId(user.getId().toString());
        event.setActive(userState != User.UserState.ACTIVE);
        event.setStatus(userState.name());
        event.setFlags(List.of());
        event.setReason(reason);
        event.setUpdatedAt(Instant.now().toString());
        hotlistEventPublisher.publish(event);
    }

    @Transactional(readOnly = true)
    public Page<AdminUserSummaryResponse> getAdminUsers(
        User.UserType type,
        User.UserState state,
        Pageable pageable
    ) {
        return userRepository.findAdminUsers(type, state, pageable)
            .map(AdminUserSummaryResponse::from);
    }

    private void publishSellerStatusEvent(UUID sellerId, SellerStatus status, String reason) {
        HotlistEventMessage event = new HotlistEventMessage();
        event.setEventId(UUID.randomUUID().toString());
        event.setSubjectType("seller");
        event.setSubjectId(sellerId.toString());
        // [1] seller 상태 자체가 필요하므로 active=true로 유지한다.
        event.setActive(true);
        event.setStatus(status.name());
        event.setFlags(List.of());
        event.setReason(reason == null || reason.isBlank() ? "SELLER_STATUS_UPDATE" : reason.trim());
        event.setUpdatedAt(Instant.now().toString());
        hotlistEventPublisher.publish(event);
    }

    private void handleLinkRequired(OAuthUserInfo userInfo) {
        // 이메일이 이미 등록되어 있으면 소셜 연결을 요구한다.
        if (userInfo.email() != null && userRepository.findByEmail(userInfo.email()).isPresent()) {
            throw new CustomException(AuthErrorCode.OAUTH_LINK_REQUIRED);
        }
    }

    private User createOAuthUser(OAuthUserInfo userInfo) {
        // JWT 인증은 이메일 기반이므로 이메일이 없으면 가입을 중단한다.
        if (userInfo.email() == null || userInfo.email().isBlank()) {
            throw new CustomException(AuthErrorCode.OAUTH_EMAIL_REQUIRED);
        }

        // 소셜 정보로 기본 사용자 엔티티를 생성한다.
        User user = User.create(
            userInfo.email(),
            normalizeName(userInfo.name(), userInfo.email()),
            userInfo.phone(),
            false
        );
        user.touchLastLogin(now());
        return userRepository.save(user);
    }

    private TokenResult issueTokens(User user) {
        // [4] 토큰 발급과 리프레시 토큰 저장을 하나의 흐름으로 묶어 캡슐화한다.
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new CustomException(AuthErrorCode.OAUTH_EMAIL_REQUIRED);
        }

        String role = resolveRole(user.getUserType());
        Map<String, Object> entitlements = buildEntitlements(user);
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), role, entitlements);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail(), role);

        refreshTokenRepository.deleteAllByUserId(user.getId());
        refreshTokenRepository.save(
            RefreshToken.issue(user.getId(), refreshToken, jwtTokenProvider.getRefreshTokenValidity()));

        return new TokenResult(user.getId(), user.getEmail(), accessToken, refreshToken);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private String normalizeName(String name, String email) {
        // 이름이 없으면 이메일을 임시 표시명으로 사용한다.
        if (name == null || name.isBlank()) {
            return email == null ? "UNKNOWN" : email;
        }
        return name;
    }
}
