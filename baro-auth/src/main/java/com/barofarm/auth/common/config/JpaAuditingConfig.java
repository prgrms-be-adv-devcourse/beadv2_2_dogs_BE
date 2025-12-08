package com.barofarm.auth.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing

// Auditing Listener 등록: 엔티티 저장(PrePersist)나 업데이터(PreUpdate) 발생할 때 가로채는 리스너를 등록
// 정의해둔 엔티티 필드에 현재 시간이나 현재 로그인 사용자 정보를 자동 주입
// 사용할 엔티티에 @EntityListeners(AuditingEntityListener.class) 추가해야
public class JpaAuditingConfig {
}
