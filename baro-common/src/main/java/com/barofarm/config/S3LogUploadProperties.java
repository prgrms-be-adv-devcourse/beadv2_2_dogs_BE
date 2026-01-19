package com.barofarm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloud.aws.s3.log-upload")
@Getter
@Setter
public class S3LogUploadProperties {

    private boolean enabled = true;
    private String localDir = "logs";
    private String keyPrefix = "logs";
}
