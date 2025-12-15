package com.barofarm.auth.infrastructure.config;

import com.barofarm.auth.infrastructure.security.JwtAuthenticationEntryPoint;
import com.barofarm.auth.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // 메소드별로 보안 설정을 활성화
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // 요청마다 JWT를 검증
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 정적 리소스만 보안 필터를 타지 않게 제외 (actuator는 아래 체인에서 별도 처리)
        return (web) -> web.ignoring()
            .requestMatchers("/webjars/**", "/favicon.ico");
    }

    @Bean
    @Order(0) // 251211: actuator 엔드포인트를 최우선으로 인증 없이 허용
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        // Actuator 전용 체인: EndpointRequest 매처로 잡히는 요청은 모두 허용
        http.securityMatcher(EndpointRequest.toAnyEndpoint())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 그 외 모든 요청은 이 체인으로 처리
        http.securityMatcher("/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1) 완전 공개 URL
                        .requestMatchers(
                            "/api/v1/auth/login",
                            "/api/v1/auth/signup",
                            "/api/v1/auth/refresh",
                            "/api/v1/auth/logout",
                            "/api/v1/auth/verification/**",
                            "/api/v1/auth/password/**",
                            "/api/v1/auth/*/grant-seller", // TODO: 이후에 삭제하고 SELLER와 내부 토큰으로
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/swagger-resources/**", "/webjars/**", "/configuration/**")
                        .permitAll()
                        // 2) 나머지는 인증 필요
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                // JWT 필터가 UsernamePasswordAuthenticationFilter보다 먼저 실행되도록 설정
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
