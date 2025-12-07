package com.barofarm.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter
    extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

  @Value("${jwt.secret:barofarm-secret-key-for-jwt-authentication-must-be-256-bits-long}")
  private String jwtSecret;

  public AuthenticationFilter() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();

      String path = request.getPath().value();
      // 스웨거 문서 호출은 인증을 건너뛰어야 401/403 없이 로드된다.
      if (path.startsWith("/api/auth/swagger-ui")
          || path.startsWith("/api/auth/v3/api-docs")
          || path.startsWith("/api/auth/swagger-resources")) {
        return chain.filter(exchange);
      }

      String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
      if (authHeader == null) {
        return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
      }
      if (!authHeader.startsWith("Bearer ")) {
        return onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
      }

      String token = authHeader.substring(7);

      try {
        Claims claims = validateToken(token);

        // 사용자 정보를 헤더에 추가
        ServerHttpRequest modifiedRequest =
            request
                .mutate()
                .header("X-User-Id", claims.getSubject())
                .header("X-User-Role", claims.get("role", String.class))
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
      } catch (Exception e) {
        return onError(exchange, "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
      }
    };
  }

  private Claims validateToken(String token) {
    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(status);
    return response.setComplete();
  }

  public static class Config {
    // Configuration properties if needed
  }
}
