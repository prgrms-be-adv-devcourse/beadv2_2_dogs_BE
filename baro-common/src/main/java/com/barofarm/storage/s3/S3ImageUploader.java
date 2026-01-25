package com.barofarm.storage.s3;

import com.barofarm.config.AwsProperties;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3ImageUploader {

    private static final Logger LOG = LoggerFactory.getLogger(S3ImageUploader.class);
    private static final String WEBP_CONTENT_TYPE = "image/webp";

    private final S3Client s3Client;
    private final AwsProperties properties;

    public S3ImageUploader(S3Client s3Client, AwsProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    public UploadedImage uploadWebpImage(String category, MultipartFile file) {
        validateImage(file);

        WebpResult webp = convertToWebp(file);
        String key = buildKey(category);

        PutObjectRequest putReq = PutObjectRequest.builder()
            .bucket(properties.getS3().getBucket())
            .key(key)
            .contentType(WEBP_CONTENT_TYPE)
            .build();

        s3Client.putObject(putReq, RequestBody.fromBytes(webp.bytes()));

        String url = buildPublicUrl(key);
        return new UploadedImage(key, url, webp.bytes().length, WEBP_CONTENT_TYPE, webp.width(), webp.height());
    }

    public void deleteObject(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.getS3().getBucket())
                .key(key)
                .build();
            s3Client.deleteObject(request);
        } catch (Exception e) {
            // 삭제 실패는 치명적이지 않다면 로그만 남기고 스킵
            // (운영에선 warn 정도가 적당)
            LOG.warn("S3 delete failed (ignored). key={}", key, e);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required.");
        }

        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid image content type.");
        }

        Long max = properties.getS3().getMaxUploadBytes();
        if (max != null && max > 0 && file.getSize() > max) {
            throw new IllegalArgumentException("Image file is too large.");
        }
    }

    private WebpResult convertToWebp(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            BufferedImage input = ImageIO.read(is);
            if (input == null) {
                throw new IllegalArgumentException("Unsupported image format.");
            }

            BufferedImage resized = resizeIfNeeded(input);
            BufferedImage rgb = ensureRgb(resized);
            byte[] bytes = writeWebp(rgb);
            return new WebpResult(bytes, rgb.getWidth(), rgb.getHeight());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to process image.", e);
        }
    }

    private BufferedImage resizeIfNeeded(BufferedImage input) {
        Integer maxWidth = properties.getS3().getMaxWidth();
        Integer maxHeight = properties.getS3().getMaxHeight();
        if (maxWidth == null || maxHeight == null || maxWidth <= 0 || maxHeight <= 0) {
            return input;
        }

        int width = input.getWidth();
        int height = input.getHeight();
        double scale = Math.min(1.0, Math.min((double) maxWidth / width, (double) maxHeight / height));
        if (scale >= 1.0) {
            return input;
        }

        int targetW = Math.max(1, (int) Math.round(width * scale));
        int targetH = Math.max(1, (int) Math.round(height * scale));

        BufferedImage resized = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(input, 0, 0, targetW, targetH, null);
        g2d.dispose();

        return resized;
    }

    private byte[] writeWebp(BufferedImage image) throws IOException {
        ImmutableImage immutable = ImmutableImage.fromAwt(image);
        WebpWriter writer = new WebpWriter();
        Float quality = properties.getS3().getWebpQuality();
        if (quality != null) {
            int q = Math.max(0, Math.min(100, Math.round(quality * 100f)));
            writer = writer.withQ(q);
        }

        byte[] bytes = immutable.bytes(writer);
        if (bytes.length == 0) {
            throw new IllegalStateException("WebP conversion produced empty bytes.");
        }
        return bytes;
    }

    private String buildKey(String category) {
        String prefix = properties.getS3().getKeyPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = "images";
        }

        String folder = prefix;
        if (category != null && !category.isBlank()) {
            folder = prefix + "/" + category;
        }

        LocalDate today = LocalDate.now();
        String datePath = "%04d/%02d/%02d".formatted(today.getYear(), today.getMonthValue(), today.getDayOfMonth());
        return "%s/%s/%s.webp".formatted(folder, datePath, UUID.randomUUID());
    }

    private String buildPublicUrl(String key) {
        String base = properties.getS3().getPublicBaseUrl();
        if (base != null && !base.isBlank()) {
            return base.endsWith("/") ? base + key : base + "/" + key;
        }
        return "https://%s.s3.amazonaws.com/%s".formatted(properties.getS3().getBucket(), key);
    }

    private BufferedImage ensureRgb(BufferedImage input) {
        if (input.getType() == BufferedImage.TYPE_INT_RGB) {
            return input;
        }
        BufferedImage rgb = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = rgb.createGraphics();
        g2d.drawImage(input, 0, 0, java.awt.Color.WHITE, null);
        g2d.dispose();
        return rgb;
    }

    public record UploadedImage(
        String key,
        String url,
        long size,
        String contentType,
        int width,
        int height
    ) {}

    private record WebpResult(byte[] bytes, int width, int height) {}
}
