package com.barofarm.opabundle.application.port;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;

// [0] OPA 번들 생성 책임을 외부로 분리한 출력 포트 인터페이스.
public interface BundleWriterPort {

    // [1] Write a bundle containing data.json and policy files.
    void writeBundle(
        Path policyDir,
        Path dataFilePath,
        Path bundlePath,
        ObjectMapper objectMapper,
        Object dataRoot
    );
}
