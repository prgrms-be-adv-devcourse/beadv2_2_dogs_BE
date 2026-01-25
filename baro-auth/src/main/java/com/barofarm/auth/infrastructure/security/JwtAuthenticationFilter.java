package com.barofarm.auth.infrastructure.security;

// JWT 인증 필터. 쿠키(HttpOnly) 또는 Authorization 헤더에서 토큰을 읽는다.
// [1] 기본은 쿠키에서 읽는다. (XSS로 localStorage 탈취 위험을 줄이기 위함)
// [2] 기존 클라이언트/서버 간 호출을 위해 Bearer 헤더도 허용한다.
// [3] 이 필터는 인증만 처리하며, 인가는 SecurityConfig에서 제어한다.

import com.barofarm.auth.infrastructure.config.AuthCookieProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthCookieProperties cookieProperties;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService customUserDetailsService,
            AuthCookieProperties cookieProperties) {
        this.tokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.cookieProperties = cookieProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        if (token != null && tokenProvider.validateToken(token)) {
            String email = tokenProvider.getEmail(token);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // [4] Authorization 헤더를 우선한다. (테스트/서버 간 호출 호환)
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        // [5] 브라우저 클라이언트는 HttpOnly 쿠키를 사용한다.
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieProperties.getAccessName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
