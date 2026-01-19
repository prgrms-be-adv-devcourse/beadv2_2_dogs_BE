package com.barofarm.log.s3;

import com.barofarm.config.AwsProperties;
import com.barofarm.config.S3LogUploadProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3LogUploader {

    private static final Logger LOG = LoggerFactory.getLogger(S3LogUploader.class);
    private static final TypeReference<Map<String, FileState>> STATE_TYPE =
        new TypeReference<>() { };

    private final S3Client s3Client;
    private final AwsProperties awsProperties;
    private final S3LogUploadProperties properties;
    private final ObjectMapper objectMapper;
    private final String serviceName;
    private final ReentrantLock lock = new ReentrantLock();
    private Map<String, FileState> state = new HashMap<>();
    private Path stateFile;

    public S3LogUploader(
        S3Client s3Client,
        AwsProperties awsProperties,
        S3LogUploadProperties properties,
        ObjectMapper objectMapper,
        Environment environment
    ) {
        this.s3Client = s3Client;
        this.awsProperties = awsProperties;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.serviceName = resolveServiceName(environment);
        initState();
    }

    @Scheduled(fixedDelayString = "${cloud.aws.s3.log-upload.interval-ms:60000}")
    public void uploadLogs() {
        if (!lock.tryLock()) {
            return;
        }
        try {
            String bucket = awsProperties.getS3().getBucket();
            if (bucket == null || bucket.isBlank()) {
                LOG.warn("S3 log upload skipped: bucket is empty");
                return;
            }

            Path logDir = Paths.get(properties.getLocalDir());
            if (!Files.isDirectory(logDir)) {
                LOG.warn("S3 log upload skipped: log dir not found {}", logDir);
                return;
            }

            try (Stream<Path> files = Files.list(logDir)) {
                files.filter(Files::isRegularFile)
                    .filter(path -> !path.equals(stateFile))
                    .forEach(path -> uploadIfChanged(bucket, path));
            }

            persistState();
        } catch (Exception e) {
            LOG.warn("S3 log upload failed", e);
        } finally {
            lock.unlock();
        }
    }

    private void uploadIfChanged(String bucket, Path path) {
        try {
            FileState current = fileState(path);
            FileState previous = state.get(path.toString());
            if (current.equals(previous)) {
                return;
            }

            String key = buildObjectKey(path);
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
            s3Client.putObject(request, RequestBody.fromFile(path));
            state.put(path.toString(), current);
        } catch (Exception e) {
            LOG.warn("S3 log upload failed for {}", path, e);
        }
    }

    private FileState fileState(Path path) throws IOException {
        long lastModified = Files.getLastModifiedTime(path).toMillis();
        long size = Files.size(path);
        return new FileState(lastModified, size);
    }

    private String buildObjectKey(Path path) {
        String prefix = normalizePrefix(properties.getKeyPrefix());
        return prefix + "/" + serviceName + "/" + path.getFileName().toString();
    }

    private String normalizePrefix(String prefix) {
        String value = prefix == null ? "" : prefix.trim();
        if (value.isEmpty()) {
            return "logs";
        }
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private void initState() {
        Path logDir = Paths.get(properties.getLocalDir());
        this.stateFile = logDir.resolve(".s3-upload-state.json");
        if (!Files.exists(stateFile)) {
            return;
        }
        try {
            state = objectMapper.readValue(stateFile.toFile(), STATE_TYPE);
        } catch (Exception e) {
            LOG.warn("Failed to read S3 log upload state: {}", stateFile, e);
        }
    }

    private void persistState() {
        try {
            Files.createDirectories(stateFile.getParent());
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(stateFile.toFile(), state);
        } catch (Exception e) {
            LOG.warn("Failed to persist S3 log upload state", e);
        }
    }

    private String resolveServiceName(Environment environment) {
        String name = environment.getProperty("spring.application.name");
        if (name == null || name.isBlank()) {
            return "unknown-service";
        }
        return name;
    }

    private static class FileState {
        public long lastModified;
        public long size;

        public FileState() {
        }

        private FileState(long lastModified, long size) {
            this.lastModified = lastModified;
            this.size = size;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FileState other)) {
                return false;
            }
            return lastModified == other.lastModified && size == other.size;
        }

        @Override
        public int hashCode() {
            int result = Long.hashCode(lastModified);
            result = 31 * result + Long.hashCode(size);
            return result;
        }
    }
}
