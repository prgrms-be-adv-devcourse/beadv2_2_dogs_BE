package com.barofarm.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("공개 URL은 필터링 없이 통과한다")
    void public_url_bypasses_filter() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Authorization 헤더 없으면 인증을 시도하지 않는다")
    void no_authorization_header() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/me");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 인증 컨텍스트를 설정하지 않는다")
    void invalid_token_does_not_authenticate() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/me");
        request.addHeader("Authorization", "Bearer invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenProvider.validateToken("invalid")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("유효한 토큰이면 SecurityContext에 Authentication을 채운다")
    void valid_token_sets_authentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/me");
        request.addHeader("Authorization", "Bearer goodtoken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenProvider.validateToken("goodtoken")).thenReturn(true);
        when(tokenProvider.getEmail("goodtoken")).thenReturn("user@example.com");
        when(customUserDetailsService.loadUserByUsername("user@example.com"))
                .thenReturn(new User("user@example.com", "", List.of()));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user@example.com");
    }
}
