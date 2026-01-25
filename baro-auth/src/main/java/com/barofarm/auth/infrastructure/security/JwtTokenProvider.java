package com.barofarm.auth.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final Key key; // JWT 서명 키(HMAC)
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-ms}") long accessTokenValidityMs,
            @Value("${jwt.refresh-token-validity-ms}") long refreshTokenValidityMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes()); // 시크릿 바이트로 키 생성
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    public String generateAccessToken(UUID userId, String email, String userType) {
        return generateAccessToken(userId, email, userType, Map.of());
    }

    public String generateAccessToken(UUID userId, String email, String userType, Map<String, Object> entitlements) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMs);

        var builder = Jwts.builder()
                .setSubject(email)
                .claim("uid", userId.toString())
                .claim("ut", userType); // Base role claim used by gateway/OPA.

        if (entitlements != null && !entitlements.isEmpty()) {
            // Extra entitlements (status/flags) for coarse-grained authorization at the gateway.
            entitlements.forEach(builder::claim);
        }

        return builder.setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId, String email, String userType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityMs);

        return Jwts.builder().setSubject(email).claim("uid", userId.toString()).claim("ut", userType)
                .claim("jti", UUID.randomUUID().toString()).setIssuedAt(now).setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public UUID getUserId(String token) {
        String uid = parseClaims(token).get("uid", String.class);
        return uid == null ? null : UUID.fromString(uid);
    }

    public Duration getRefreshTokenValidity() {
        return Duration.ofMillis(refreshTokenValidityMs);
    }

    public Duration getAccessTokenValidity() {
        return Duration.ofMillis(accessTokenValidityMs);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
