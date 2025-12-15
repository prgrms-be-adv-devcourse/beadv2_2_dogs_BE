package com.barofarm.seller.config.S3;

import com.barofarm.seller.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static com.barofarm.seller.farm.exception.FarmErrorCode.*;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.public-base-url}")
    private String publicBaseUrl;

    public UploadedObject uploadFarmImage(UUID farmId, MultipartFile file) {
        validateImage(file);

        String ext = extractExt(file.getOriginalFilename());
        String key = "farms/%s/%s.%s".formatted(farmId, UUID.randomUUID(), ext);

        try (InputStream is = file.getInputStream()) {
            PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

            s3Client.putObject(putReq, RequestBody.fromInputStream(is, file.getSize()));

            String url = buildPublicUrl(key);
            return new UploadedObject(key, url, file.getSize(), file.getContentType());

        } catch (IOException e) {
            throw new CustomException(FARM_IMAGE_UPLOAD_FAIL);
        } catch (Exception e) {
            throw new CustomException(FARM_IMAGE_UPLOAD_FAIL);
        }
    }

    public void deleteObject(String key) {
        try {
            DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

            s3Client.deleteObject(deleteReq);
        } catch (Exception e) {
            // 실무에서는 로그만 남기고 흘리는 경우가 많음
            throw new CustomException(FARM_IMAGE_DELETE_FAIL);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null) {
            throw new CustomException(FARM_IMAGE_REQUIRED);
        }

        if (file.isEmpty()) {
            throw new CustomException(FARM_IMAGE_EMPTY_FILE);
        }

        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new CustomException(FARM_IMAGE_INVALID_TYPE);
        }

        long max = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > max) {
            throw new CustomException(FARM_IMAGE_TOO_LARGE);
        }
    }

    private String extractExt(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String buildPublicUrl(String key) {
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return publicBaseUrl.endsWith("/")
                ? publicBaseUrl + key
                : publicBaseUrl + "/" + key;
        }
        return "https://%s.s3.amazonaws.com/%s".formatted(bucket, key);
    }

    public record UploadedObject(String key, String url, long size, String contentType) {}
}
