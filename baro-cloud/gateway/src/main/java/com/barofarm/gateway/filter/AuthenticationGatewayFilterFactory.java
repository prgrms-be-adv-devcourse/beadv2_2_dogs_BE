package com.barofarm.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
 * AuthenticationGatewayFilterFactory: 인증(Authentication) 담당.
 * - JWT 토큰을 검증하고, 사용자 정보를 헤더로 주입한다.
 * - 결과적으로 OPA가 사용할 수 있게 X-User-Id, X-User-Role,
 *   X-User-Status, X-User-Flags 헤더를 설정한다.
 * - 즉, 요청자의 인증 여부와 속성 파악을 위한 전처리 단계.
 */
@Component
public class AuthenticationGatewayFilterFactory
    extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    private static final String HEADER_USER_STATUS = "X-User-Status";
    private static final String HEADER_SELLER_STATUS = "X-Seller-Status";
    private static final String HEADER_USER_FLAGS = "X-User-Flags";

    private final String jwtSecret;

    public AuthenticationGatewayFilterFactory(
        @Value("${jwt.secret:barofarm-secret-key-for-jwt-authentication-must-be-256-bits-long}")
        String jwtSecret
    ) {
        super(Config.class);
        this.jwtSecret = jwtSecret;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            // [1] 브라우저는 HttpOnly 쿠키를 사용하므로, gateway에서 Bearer 헤더로 변환해준다.
            if (authHeader == null) {
                var cookie = request.getCookies().getFirst("access_token");
                if (cookie != null && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    authHeader = "Bearer " + cookie.getValue();
                }
            }
            if (authHeader == null) {
                return chain.filter(exchange);
            }
            if (!authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = validateToken(token);

                String email = claims.getSubject();
                String userId = claims.get("uid", String.class);
                String userType = claims.get("ut", String.class);
                // Optional entitlement claims for coarse-grained authorization.
                String userStatus = getClaimAsString(claims, "us", "user_status", "userStatus");
                String sellerStatus = getClaimAsString(claims, "ss", "seller_status", "sellerStatus");
                List<String> flags = getClaimAsStringList(claims, "flags");

                ServerHttpRequest.Builder builder = request.mutate()
                    .headers(h -> {
                        h.remove("X-User-Email");
                        h.remove("X-User-Id");
                        h.remove("X-User-Role");
                        h.remove(HEADER_USER_STATUS);
                        h.remove(HEADER_SELLER_STATUS);
                        h.remove(HEADER_USER_FLAGS);
                    })
                    .header("X-User-Email", email)
                    .header("X-User-Id", userId)
                    .header("X-User-Role", userType);

                if (userStatus != null && !userStatus.isBlank()) {
                    builder.header(HEADER_USER_STATUS, userStatus);
                }
                if (sellerStatus != null && !sellerStatus.isBlank()) {
                    builder.header(HEADER_SELLER_STATUS, sellerStatus);
                }
                if (!flags.isEmpty()) {
                    // Comma-separated flags are forwarded to OPA.
                    builder.header(HEADER_USER_FLAGS, String.join(",", flags));
                }

                ServerHttpRequest modifiedRequest = builder.build();
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

    private static String getClaimAsString(Claims claims, String... keys) {
        for (String key : keys) {
            Object value = claims.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    private static List<String> getClaimAsStringList(Claims claims, String key) {
        Object value = claims.get(key);
        if (value == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        if (value instanceof Collection<?> collection) {
            for (Object item : collection) {
                if (item == null) {
                    continue;
                }
                String text = item.toString().trim();
                if (!text.isEmpty()) {
                    result.add(text);
                }
            }
            return result;
        }

        String raw = value.toString().trim();
        if (raw.isEmpty()) {
            return List.of();
        }
        for (String part : raw.split(",")) {
            String text = part.trim();
            if (!text.isEmpty()) {
                result.add(text);
            }
        }
        return result;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    public static class Config {
    }
}
