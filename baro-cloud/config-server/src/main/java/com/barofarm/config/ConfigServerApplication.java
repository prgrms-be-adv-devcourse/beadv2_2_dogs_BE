package com.barofarm.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server 애플리케이션. [1] 중앙 집중형 설정 저장소: 각 마이크로서비스가 부트스트랩 시점에 yml/properties를 가져온다.
 * [2] @EnableConfigServer가 /{app}/{profile} 엔드포인트를 열어 Git/native 저장소의 설정을 노출한다. [3] Eureka
 * 등록(@EnableDiscoveryClient)으로 설정 서버가 수평 확장되더라도 게이트웨이/서비스가 자동으로 위치를 해석한다.
 */
@SpringBootApplication // [공통] 컴포넌트 스캔 + 자동설정 부트스트랩
@EnableConfigServer // [2] Config Server 역할 활성화
@EnableDiscoveryClient // [3] 서비스 레지스트리에 자신을 등록
public class ConfigServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ConfigServerApplication.class, args);
  }
}
