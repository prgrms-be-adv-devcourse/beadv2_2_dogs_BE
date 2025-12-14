package com.barofarm.buyer.common.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
@Order(1)
public class LoggingFilter extends OncePerRequestFilter implements Filter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 모든 요청을 필터링하도록 false 반환
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // POST 요청의 body를 안전하게 읽기 위해 ContentCachingRequestWrapper 사용
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        
        String method = wrappedRequest.getMethod();
        String uri = wrappedRequest.getRequestURI();
        String queryString = wrappedRequest.getQueryString();
        String fullUrl = queryString == null ? uri : uri + "?" + queryString;
        
        // 필터 진입 확인 - 무조건 출력 (POST 요청 확인용)
        System.err.println("🔍 [FILTER_ENTER] " + method + " " + fullUrl);
        System.out.println("🔍 [FILTER_ENTER] " + method + " " + fullUrl);
        log.error("🔍 [FILTER_ENTER] {} {}", method, fullUrl);
        log.warn("🔍 [FILTER_ENTER] {} {}", method, fullUrl);
        log.info("🔍 [FILTER_ENTER] {} {}", method, fullUrl);
        
        // 필터 체인 정보 확인
        if ("POST".equals(method)) {
            String filterChainInfo = filterChain.getClass().getName();
            System.err.println("🔗 [FILTER_CHAIN] " + filterChainInfo);
            log.info("🔗 [FILTER_CHAIN] {}", filterChainInfo);
        }
        
        // 필터 체인 정보 확인 (첫 요청 시에만)
        if (log.isDebugEnabled() || "POST".equals(method)) {
            try {
                jakarta.servlet.FilterChain filterChainObj = filterChain;
                String filterChainInfo = filterChainObj.getClass().getName();
                System.err.println("🔗 [FILTER_CHAIN] " + filterChainInfo);
                log.info("🔗 [FILTER_CHAIN] {}", filterChainInfo);
            } catch (Exception e) {
                // 필터 체인 정보 출력 실패는 무시
            }
        }
        
        // 모든 요청 로깅 (INFO 레벨로 강제) - System.out과 log 모두 사용
        System.err.println("🌐 [HTTP_REQUEST] " + method + " " + fullUrl + " - Remote: " + wrappedRequest.getRemoteAddr());
        System.out.println("🌐 [HTTP_REQUEST] " + method + " " + fullUrl + " - Remote: " + wrappedRequest.getRemoteAddr());
        log.error("🌐 [HTTP_REQUEST] {} {} - Remote: {}", method, fullUrl, wrappedRequest.getRemoteAddr());
        log.warn("🌐 [HTTP_REQUEST] {} {} - Remote: {}", method, fullUrl, wrappedRequest.getRemoteAddr());
        log.info("🌐 [HTTP_REQUEST] {} {} - Remote: {}", method, fullUrl, wrappedRequest.getRemoteAddr());
        
        try {
            // 모든 요청 처리
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            
            // POST/PUT/PATCH 요청의 경우 body 로깅
            if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
                System.err.println("📝 [POST_REQUEST] Processing POST/PUT/PATCH request: " + fullUrl);
                System.out.println("📝 [POST_REQUEST] Processing POST/PUT/PATCH request: " + fullUrl);
                log.info("📝 [POST_REQUEST] Processing POST/PUT/PATCH request: {}", fullUrl);
                
                // 필터 체인 실행 후 body 읽기 (이미 컨트롤러에서 읽었으므로 안전)
                byte[] contentAsByteArray = wrappedRequest.getContentAsByteArray();
                if (contentAsByteArray.length > 0) {
                    String requestBody = new String(contentAsByteArray, wrappedRequest.getCharacterEncoding());
                    System.err.println("📦 [REQUEST_BODY] " + requestBody);
                    System.out.println("📦 [REQUEST_BODY] " + requestBody);
                    log.info("📦 [REQUEST_BODY] {}", requestBody);
                }
            }
            
            // Response body 복사 (모든 요청에 대해 필요)
            wrappedResponse.copyBodyToResponse();
            
            // 응답 로깅
            System.err.println("✅ [HTTP_RESPONSE] " + method + " " + fullUrl + " - Status: " + wrappedResponse.getStatus());
            System.out.println("✅ [HTTP_RESPONSE] " + method + " " + fullUrl + " - Status: " + wrappedResponse.getStatus());
            log.error("✅ [HTTP_RESPONSE] {} {} - Status: {}", method, fullUrl, wrappedResponse.getStatus());
            log.warn("✅ [HTTP_RESPONSE] {} {} - Status: {}", method, fullUrl, wrappedResponse.getStatus());
            log.info("✅ [HTTP_RESPONSE] {} {} - Status: {}", method, fullUrl, wrappedResponse.getStatus());
        } catch (Exception e) {
            System.err.println("❌ [HTTP_ERROR] " + method + " " + fullUrl + " - Error: " + e.getMessage());
            System.err.println("❌ [HTTP_ERROR] StackTrace: ");
            e.printStackTrace(System.err);
            log.error("❌ [HTTP_ERROR] {} {} - Error: {}", method, fullUrl, e.getMessage(), e);
            throw e;
        }
    }
}

