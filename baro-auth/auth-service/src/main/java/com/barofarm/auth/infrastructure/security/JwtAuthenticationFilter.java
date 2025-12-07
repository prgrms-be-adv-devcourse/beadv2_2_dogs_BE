package com.barofarm.auth.infrastructure.security;

// 헤더 확인하고 검증 필터

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

  private final JwtTokenProvider tokenProvider; // 헬퍼
  private final CustomUserDetailsService customUserDetailsService; // 이메일로 사용자 정보 불러오는 서비스

  public JwtAuthenticationFilter(
      JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
    this.tokenProvider = jwtTokenProvider;
    this.customUserDetailsService = customUserDetailsService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String requestURI = request.getRequestURI();

    // 0-1) Swagger & 공개 URL은 아예 필터에서 건너뛰기 [개발과정에선]
    if (requestURI.startsWith("/swagger-ui")
        || requestURI.startsWith("/v3/api-docs")
        || requestURI.startsWith("/auth/login")
        || requestURI.startsWith("/auth/signup")
        || requestURI.startsWith("/auth/verification")) {
      filterChain.doFilter(request, response);
      return;
    }

    // 0-2) Authorization 헤더가 없으면 그냥 통과 (익명 사용자)
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    // 1. 헤더 값 가져오기
    String bearer = request.getHeader("Authorization"); // 헤더 가져오기

    // 2. Auth 헤더 존재하는지, 값이 "Bearer "로 시작하는지 확인
    if (bearer != null && bearer.startsWith("Bearer ")) {
      // "Bearer " 이후 문자열 잘라서 가져오기
      String token = bearer.substring(7);

      // 3. 토큰 유효한지
      if (tokenProvider.validateToken(token)) {
        // 4. 식별자인 이메일 가져오기
        String email = tokenProvider.getEmail(token);

        // 5. userDetail서비스로 실제 사용자 정보 가져오기
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // 6. 스프링.시큐에서 쓸 auth객체 만들기
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // 7. 현재 요청(request)에 대한 추가 상세 정보를 Authentication에 넣기
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 8. 스프링 시큐리티 컨텍스트에 "인증됨"알려주기
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
      // 여기 빠져나오고 토큰 없으면 시큐.컨텍 비어있고 -> 그럼 인증 x

    }
    // 9. 필터 체인에서 다음 단계 처리
    filterChain.doFilter(request, response);
  }
}
