package com.barofarm.auth.domain.oauth;

/**
 * OAuth 공급자에서 받은 사용자 핵심 정보.
 * 도메인 로직은 이 값 객체에 의존하고, 외부 응답 포맷은 인프라 계층에서 변환한다.
 */
public record OAuthUserInfo(
    OAuthProvider provider,
    String providerUserId,
    String email,
    String name,
    String phone
) {
}
