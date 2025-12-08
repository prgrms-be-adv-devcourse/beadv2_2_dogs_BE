package com.barofarm.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server. [1] 모든 마이크로서비스 인스턴스를 등록/조회하는 중앙 레지스트리. [2] 게이트웨이에서 lb://{serviceId} 형태로 서비스를 찾을 때
 * 이 레지스트리를 사용한다.
 */
@SpringBootApplication // [공통] 컴포넌트 스캔/자동설정
@EnableEurekaServer // [1] Eureka 대시보드 및 등록/조회 엔드포인트 활성화
public class EurekaServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(EurekaServerApplication.class, args);
  }
}
