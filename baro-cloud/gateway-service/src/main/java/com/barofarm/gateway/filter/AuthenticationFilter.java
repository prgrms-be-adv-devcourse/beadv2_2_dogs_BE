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

/**
 * JWT 인증 필터. [1] Gateway 라우트에서 AuthenticationFilter를 적용한 요청만 통과하며, Authorization 헤더의 Bearer 토큰을
 * 검증한다. [2] 검증된 사용자 식별자/역할을 X-User-* 헤더로 Downstream 서비스에 전달해 추가 인증 로직을 단순화한다. [3] Swagger 같은 공개 경로는
 * 필터를 우회하여 개발 편의를 보장한다.
 */
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
      // [3] 문서/테스트 경로는 인증 없이 접근 가능하도록 즉시 통과(모든 서비스 공통 Swagger 경로)
      if (isSwaggerPath(path)) {
        return chain.filter(exchange);
      }

      String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
      // [1] 인증 헤더가 없거나 Bearer 스킴이 아니면 즉시 401 반환
      if (authHeader == null) {
        return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
      }
      if (!authHeader.startsWith("Bearer ")) {
        return onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
      }

      String token = authHeader.substring(7);

      try {
        Claims claims = validateToken(token); // [1] 서명 검증 + 만료 확인

        // [2] Downstream 서비스가 재파싱 없이 사용자 정보를 활용하도록 헤더에 주입
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
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload(); // [1] HMAC-SHA256 서명 검증 및 클레임 추출
  }

  private boolean isSwaggerPath(String path) {
    return path.contains("/swagger-ui")
        || path.contains("/v3/api-docs")
        || path.contains("/swagger-resources");
  }

  private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(status);
    return response.setComplete(); // [1] 바디 없이 상태 코드만 반환(게이트웨이 표준)
  }

  public static class Config {
    // Configuration properties if needed
  }
}
