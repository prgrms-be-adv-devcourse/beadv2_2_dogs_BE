package com.barofarm.auth.presentation;

import com.barofarm.auth.infrastructure.config.AuthCookieProperties;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;

public class CookieUtil {

    private CookieUtil() {
    }

    // [1] HttpOnly 쿠키로 토큰을 내려 JS 접근을 차단해 XSS 탈취 위험을 줄인다.
    public static ResponseCookie accessTokenCookie(
        String token,
        Duration maxAge,
        AuthCookieProperties props
    ) {
        return buildCookie(props.getAccessName(), token, maxAge, props);
    }

    // [2] Refresh 토큰도 쿠키로만 전달해 로컬스토리지 저장을 피한다.
    public static ResponseCookie refreshTokenCookie(
        String token,
        Duration maxAge,
        AuthCookieProperties props
    ) {
        return buildCookie(props.getRefreshName(), token, maxAge, props);
    }

    public static ResponseCookie clearAccessToken(AuthCookieProperties props) {
        return buildCookie(props.getAccessName(), "", Duration.ZERO, props);
    }

    public static ResponseCookie clearRefreshToken(AuthCookieProperties props) {
        return buildCookie(props.getRefreshName(), "", Duration.ZERO, props);
    }

    public static String getCookieValue(jakarta.servlet.http.Cookie[] cookies, String name) {
        if (cookies == null) {
            return null;
        }
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static ResponseCookie buildCookie(
        String name,
        String value,
        Duration maxAge,
        AuthCookieProperties props
    ) {
        // [3] SameSite는 CSRF 완화, Secure는 HTTPS 전송 강제에 사용된다.
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(props.isSecure())
            .path(props.getPath())
            .sameSite(props.getSameSite())
            .maxAge(maxAge);
        if (StringUtils.hasText(props.getDomain())) {
            builder.domain(props.getDomain());
        }
        return builder.build();
    }
}
