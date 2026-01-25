package com.barofarm.opabundle;

import com.barofarm.opabundle.config.OpaAccessProperties;
import com.barofarm.opabundle.config.OpaBundleProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

//TODO : 향후 분리 -> bundle처리와 hotlist 서버 따로
// 이유 : 번들 생성 및 서빙은 Git 커밋/배포 단위로 움직이지만 카프카 소비는 상시 스트리밍 처리
// 이유 : 스케일링 단위가 꼬입(번들 서버는 트래픽 많아도 cpu적게 쓰지만 consumer는 이벤트량에 따라서 수평 확장 필요)

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableConfigurationProperties({OpaBundleProperties.class, OpaAccessProperties.class})
// [0] OPA 번들 서비스의 Spring Boot 애플리케이션 엔트리 클래스.
public class OpaBundleApplication {

    // [1] Spring Boot entrypoint for the OPA bundle service.
    public static void main(String[] args) {
        SpringApplication.run(OpaBundleApplication.class, args);
    }
}
