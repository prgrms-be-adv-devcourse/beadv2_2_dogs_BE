package com.barofarm.opabundle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OPA 번들 생성 경로/파일명을 바인딩하는 설정 클래스.
 * 클린 아키텍처 관점에서는 Spring 설정(프레임워크/인프라) 계층에 해당.
 * 비즈니스 규칙을 담지 않고 런타임 구성값만 보관.
 */
@ConfigurationProperties(prefix = "opa.bundle")
// [0] OPA 번들 생성/출력 경로 설정을 보관하는 설정 프로퍼티 클래스.
public class OpaBundleProperties {

    // [1] 번들에 포함할 rego/정책 파일의 루트 디렉터리 경로.
    private String policyDir;
    // [2] 생성된 번들과 데이터 파일을 저장할 출력 디렉터리 경로.
    private String outputDir;
    // [3] 번들 압축 파일 이름 (예: bundle.tar.gz).
    private String bundleFileName;
    // [4] 번들과 함께 생성되는 데이터 JSON 파일 이름.
    private String dataFileName;

    public String getPolicyDir() {
        return policyDir;
    }

    public void setPolicyDir(String policyDir) {
        this.policyDir = policyDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getBundleFileName() {
        return bundleFileName;
    }

    public void setBundleFileName(String bundleFileName) {
        this.bundleFileName = bundleFileName;
    }

    public String getDataFileName() {
        return dataFileName;
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }
}
