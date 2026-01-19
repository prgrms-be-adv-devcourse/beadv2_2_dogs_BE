package com.barofarm.config;

import com.barofarm.log.s3.S3LogUploader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.services.s3.S3Client;

@AutoConfiguration
@ConditionalOnClass(S3Client.class)
@ConditionalOnBean(S3Client.class)
@ConditionalOnProperty(prefix = "cloud.aws.s3.log-upload", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(S3LogUploadProperties.class)
public class S3LogUploadAutoConfiguration {

    @Bean
    public S3LogUploader s3LogUploader(
        S3Client s3Client,
        AwsProperties awsProperties,
        S3LogUploadProperties properties,
        ObjectMapper objectMapper,
        Environment environment
    ) {
        return new S3LogUploader(s3Client, awsProperties, properties, objectMapper, environment);
    }
}
