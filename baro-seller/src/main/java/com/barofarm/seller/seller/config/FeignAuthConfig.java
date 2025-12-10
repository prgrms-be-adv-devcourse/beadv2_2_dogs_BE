package com.barofarm.seller.seller.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignAuthConfig {

    @Bean
    public RequestInterceptor authForwardingInterceptor() {
        return template -> {
            // 현재 HTTP 요청 가지고 오기 (seller-service로 들어온 요청)
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if(attrs == null) {
                return; // http요청 아니면 그냥 패스하기
            }

            HttpServletRequest request = attrs.getRequest();

            // 원래 들어왔더 Authorization 헤더 읽기
            String authorization = request.getHeader("Authorization");

            // 헤더 auth부분 비어있거나 Bearer아니면 그냥 패스
            if(authorization == null || !authorization.startsWith("Bearer ")) {
                return;
            }

            template.header("Authorization", authorization);
        };
    }
}
