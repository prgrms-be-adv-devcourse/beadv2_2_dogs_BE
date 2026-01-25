package com.barofarm.opabundle.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OPA 번들 서비스의 내부 접근 제어 설정을 바인딩하는 설정 클래스.
 * 클린 아키텍처 관점에서는 Spring 설정(프레임워크/인프라) 계층에 해당.
 * 비즈니스 규칙을 담지 않고 런타임 구성값만 보관.
 */
@ConfigurationProperties(prefix = "opa.access")
// [0] 번들 접근 제어(내부망/토큰) 설정을 보관하는 설정 프로퍼티 클래스.
public class OpaAccessProperties {

    // [1] 번들 엔드포인트에 대한 내부망 전용 접근 제한을 켤지 여부.
    //     true면 IP/토큰 검사를 수행하고, false면 검사를 건너뜁니다.
    private boolean enabled = true;
    // [2] 허용 CIDR 목록이 비어 있을 때 루프백/사설 IPv4 대역을
    //     기본적으로 허용할지 여부.
    private boolean allowPrivate = true;
    // [3] 허용할 클라이언트 IP CIDR 목록.
    //     비어 있으면 allowPrivate 규칙만 적용됩니다.
    private List<String> allowedCidrs = new ArrayList<>();
    // [4] IP 검사 외에 공유 토큰까지 요구할지 여부.
    private boolean requireToken = false;
    // [5] 요청 헤더 X-Internal-Token 에서 기대하는 공유 토큰 값.
    private String token;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAllowPrivate() {
        return allowPrivate;
    }

    public void setAllowPrivate(boolean allowPrivate) {
        this.allowPrivate = allowPrivate;
    }

    public List<String> getAllowedCidrs() {
        return allowedCidrs;
    }

    public void setAllowedCidrs(List<String> allowedCidrs) {
        this.allowedCidrs = allowedCidrs;
    }

    public boolean isRequireToken() {
        return requireToken;
    }

    public void setRequireToken(boolean requireToken) {
        this.requireToken = requireToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
