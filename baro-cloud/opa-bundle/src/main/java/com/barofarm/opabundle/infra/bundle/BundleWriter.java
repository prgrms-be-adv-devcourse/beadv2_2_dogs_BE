package com.barofarm.opabundle.infra.bundle;

import com.barofarm.opabundle.application.port.BundleWriterPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
// [0] OPA 번들 아카이브를 생성하는 파일 출력/패키징 클래스.
public class BundleWriter implements BundleWriterPort {

    private static final Logger LOG = LoggerFactory.getLogger(BundleWriter.class);

    // [1] Write data.json and policy files into a gzipped tar bundle.
    @Override
    public void writeBundle(
        Path policyDir,
        Path dataFilePath,
        Path bundlePath,
        ObjectMapper objectMapper,
        Object dataRoot
    ) {
        try {
            if (bundlePath.getParent() != null) {
                Files.createDirectories(bundlePath.getParent());
            }
            if (dataFilePath.getParent() != null) {
                Files.createDirectories(dataFilePath.getParent());
            }

            mergeStaticHotlist(policyDir, objectMapper, dataRoot);
            mergeStaticPolicyData(policyDir, objectMapper, dataRoot);
            byte[] dataBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(dataRoot);
            Files.write(dataFilePath, dataBytes);

            try (OutputStream fileOut = Files.newOutputStream(bundlePath);
                GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(fileOut);
                TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut)) {
                tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
                addBytesEntry(tarOut, "data.json", dataBytes);
                addPolicyFiles(tarOut, policyDir);
            }
        } catch (IOException e) {
            LOG.error("Failed to write OPA bundle {}", bundlePath, e);
        }
    }

    // [2] Add all policy files under the policy directory to the archive.
    private void addPolicyFiles(TarArchiveOutputStream tarOut, Path policyDir) throws IOException {
        if (policyDir == null || !Files.exists(policyDir)) {
            LOG.warn("Policy directory not found: {}", policyDir);
            return;
        }
        try (var paths = Files.walk(policyDir)) {
            paths.filter(Files::isRegularFile)
                .forEach(path -> {
                String relativePath = policyDir.relativize(path).toString().replace("\\", "/");
                if (relativePath.startsWith("tests/")) {
                    return;
                }
                String entryName = resolveEntryName(relativePath);
                if (entryName == null) {
                    return;
                }
                    try {
                        TarArchiveEntry entry = new TarArchiveEntry(entryName);
                        entry.setSize(Files.size(path));
                        tarOut.putArchiveEntry(entry);
                        Files.copy(path, tarOut);
                        tarOut.closeArchiveEntry();
                    } catch (IOException e) {
                        LOG.warn("Failed to add policy file to bundle: {}", path, e);
                    }
                });
        }
    }

    private void mergeStaticHotlist(Path policyDir, ObjectMapper objectMapper, Object dataRoot) {
        if (!(dataRoot instanceof Map<?, ?> rootMap)) {
            return;
        }
        Map<String, Object> root = castStringObjectMap(rootMap);
        Map<String, Object> hotlist = getOrCreateMap(root, "hotlist");
        Map<String, Object> users = getOrCreateMap(hotlist, "users");
        Map<String, Object> sellers = getOrCreateMap(hotlist, "sellers");

        mergeHotlistFile(policyDir, objectMapper, "users", users);
        mergeHotlistFile(policyDir, objectMapper, "sellers", sellers);
    }

    private void mergeHotlistFile(
        Path policyDir,
        ObjectMapper objectMapper,
        String key,
        Map<String, Object> target
    ) {
        if (policyDir == null) {
            return;
        }
        Path filePath = policyDir.resolve("data").resolve("hotlist").resolve(key + ".json");
        if (!Files.exists(filePath)) {
            return;
        }
        try {
            Map<?, ?> parsed = objectMapper.readValue(filePath.toFile(), Map.class);
            Object node = parsed.get(key);
            if (!(node instanceof Map<?, ?> sourceMap)) {
                return;
            }
            sourceMap.forEach((entryKey, entryValue) -> {
                if (entryKey != null && !target.containsKey(entryKey.toString())) {
                    target.put(entryKey.toString(), entryValue);
                }
            });
        } catch (IOException e) {
            LOG.warn("Failed to merge static hotlist data from {}", filePath, e);
        }
    }

    private void mergeStaticPolicyData(
        Path policyDir,
        ObjectMapper objectMapper,
        Object dataRoot
    ) {
        if (!(dataRoot instanceof Map<?, ?> rootMap) || policyDir == null) {
            return;
        }
        Path dataDir = policyDir.resolve("data");
        if (!Files.exists(dataDir)) {
            return;
        }
        Map<String, Object> root = castStringObjectMap(rootMap);
        try (var paths = Files.walk(dataDir)) {
            paths.filter(Files::isRegularFile)
                .forEach(path -> {
                    String relativePath = dataDir.relativize(path).toString().replace("\\", "/");
                    if (!relativePath.endsWith(".json") || relativePath.startsWith("hotlist/")) {
                        return;
                    }
                    String withoutExt = relativePath.substring(0, relativePath.length() - ".json".length());
                    String[] segments = withoutExt.split("/");
                    if (segments.length == 0) {
                        return;
                    }
                    try {
                        Map<?, ?> parsed = objectMapper.readValue(path.toFile(), Map.class);
                        Map<String, Object> cursor = root;
                        for (int i = 0; i < segments.length - 1; i++) {
                            cursor = getOrCreateMap(cursor, segments[i]);
                        }
                        cursor.put(segments[segments.length - 1], castStringObjectMap(parsed));
                    } catch (IOException e) {
                        LOG.warn("Failed to merge policy data from {}", path, e);
                    }
                });
        } catch (IOException e) {
            LOG.warn("Failed to scan policy data directory {}", dataDir, e);
        }
    }

    private Map<String, Object> castStringObjectMap(Map<?, ?> input) {
        if (input instanceof Map<?, ?> && input.keySet().stream().allMatch(key -> key instanceof String)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> casted = (Map<String, Object>) input;
            return casted;
        }
        Map<String, Object> output = new HashMap<>();
        input.forEach((key, value) -> {
            if (key != null) {
                output.put(key.toString(), value);
            }
        });
        return output;
    }

    private Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
        Object value = parent.get(key);
        if (value instanceof Map<?, ?> mapValue) {
            Map<String, Object> casted = castStringObjectMap(mapValue);
            if (casted != value) {
                parent.put(key, casted);
            }
            return casted;
        }
        Map<String, Object> created = new HashMap<>();
        parent.put(key, created);
        return created;
    }

    private String resolveEntryName(String relativePath) {
        String lowerPath = relativePath.toLowerCase(Locale.ROOT);
        if (lowerPath.endsWith(".rego")) {
            return "policy/" + relativePath;
        }
        if (lowerPath.endsWith(".json") && relativePath.startsWith("data/")) {
            String dataRelative = relativePath.substring("data/".length());
            if (dataRelative.startsWith("hotlist/")) {
                return null;
            }
            return "data/" + dataRelative;
        }
        return null;
    }

    // [3] Add an in-memory entry (e.g. data.json) to the archive.
    private void addBytesEntry(TarArchiveOutputStream tarOut, String name, byte[] bytes) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(bytes.length);
        tarOut.putArchiveEntry(entry);
        tarOut.write(bytes);
        tarOut.closeArchiveEntry();
    }
}
