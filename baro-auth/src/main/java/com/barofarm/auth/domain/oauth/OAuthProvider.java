package com.barofarm.auth.domain.oauth;

/**
 * 지원하는 OAuth 공급자 타입.
 * enum으로 고정해두면 공급자 분기 로직이 타입 안전하게 유지된다.
 */
public enum OAuthProvider {
    // 지원하는 OAuth 공급자 목록
    NAVER,
    KAKAO;

    public static OAuthProvider from(String value) {
        // 입력 문자열을 provider enum으로 안전하게 변환한다.
        if (value == null) {
            return null;
        }
        for (OAuthProvider provider : values()) {
            if (provider.name().equalsIgnoreCase(value)) {
                return provider;
            }
        }
        return null;
    }
}
