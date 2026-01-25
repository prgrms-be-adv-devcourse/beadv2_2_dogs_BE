package com.barofarm.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication // [1] Gateway 부트스트랩 설정과 컴포넌트 스캔
@EnableDiscoveryClient // [2] Eureka 등록으로 lb:// 라우팅 활성화
public class GatewayApplication {

    /**
     * Gateway Service.
     * [1] 외부 요청을 수신하고 서비스별 경로로 라우팅한다.
     * [2] Eureka 등록 후 서비스명 기반(lb://) 라우팅을 사용한다.
     * [3] 인증/인가 필터를 통과한 요청만 백엔드로 전달한다.
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
