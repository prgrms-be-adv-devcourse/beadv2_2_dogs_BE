package com.barofarm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloud.aws")
@Getter
@Setter
public class AwsProperties {

    private String region;
    private final Credentials credentials = new Credentials();
    private final S3 s3 = new S3();

    @Getter
    public static class Credentials {
        private String accessKey;
        private String secretKey;
    }

    @Getter
    public static class S3 {
        private String bucket;
        private String publicBaseUrl;
        private String keyPrefix = "images";
        private Long maxUploadBytes = 10L * 1024 * 1024;
        private Integer maxWidth = 1280;
        private Integer maxHeight = 1280;
        private Float webpQuality = 0.85f;
    }
}
