package com.barofarm.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Deprecated
@Component
public class AuthenticationFilter extends AuthenticationGatewayFilterFactory {

    public AuthenticationFilter(
        @Value("${jwt.secret:barofarm-secret-key-for-jwt-authentication-must-be-256-bits-long}")
        String jwtSecret
    ) {
        super(jwtSecret);
    }
}
