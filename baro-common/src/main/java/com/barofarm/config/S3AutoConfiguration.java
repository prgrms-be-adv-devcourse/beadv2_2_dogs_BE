package com.barofarm.config;

import com.barofarm.storage.s3.S3ImageUploader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@AutoConfiguration
@ConditionalOnClass(S3Client.class)
@ConditionalOnProperty(prefix = "cloud.aws.s3", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AwsProperties.class)
public class S3AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(S3Client.class)
    public S3Client s3Client(AwsProperties properties) {
        S3ClientBuilder builder = S3Client.builder();
        if (properties.getRegion() != null && !properties.getRegion().isBlank()) {
            builder.region(Region.of(properties.getRegion()));
        }

        String accessKey = properties.getCredentials().getAccessKey();
        String secretKey = properties.getCredentials().getSecretKey();
        if (accessKey != null && !accessKey.isBlank()
            && secretKey != null && !secretKey.isBlank()) {
            builder.credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            );
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnBean(S3Client.class)
    @ConditionalOnProperty(prefix = "cloud.aws.s3", name = "enabled", havingValue = "true")
    public S3ImageUploader s3ImageUploader(S3Client s3Client, AwsProperties properties) {
        return new S3ImageUploader(s3Client, properties);
    }
}
