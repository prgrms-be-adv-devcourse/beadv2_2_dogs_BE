package com.barofarm.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Gateway Service. [1] 외부 트래픽의 단일 진입점: 라우팅/필터 체인으로 API 요청을 분배하고 정책을 적용한다. [2] Eureka 기반 서비스
 * 발견(lb://)으로 인스턴스 증감/변경을 코드 수정 없이 흡수한다. [3] 인증·로깅·레이트리밋 등 교차 관심사를 중앙에서 처리하기 위한 위치.
 */
@SpringBootApplication // [공통] 컴포넌트 스캔/자동설정
@EnableDiscoveryClient // [2] Eureka와 통신하여 서비스 이름을 실제 인스턴스로 해석
public class GatewayServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(GatewayServiceApplication.class, args);
  }
}
