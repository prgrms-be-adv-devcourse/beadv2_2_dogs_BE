package com.barofarm.opabundle.application;

import com.barofarm.opabundle.application.dto.HotlistEvent;
import com.barofarm.opabundle.application.port.BundleWriterPort;
import com.barofarm.opabundle.application.port.HotlistStore;
import com.barofarm.opabundle.config.OpaBundleProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
// [0] Hotlist 상태를 갱신하고 OPA 번들을 생성/갱신하는 핵심 서비스 클래스.
public class OpaBundleService {

    private static final Logger LOG = LoggerFactory.getLogger(OpaBundleService.class);

    // [1] Service orchestrating hotlist updates and bundle creation.
    private final OpaBundleProperties properties;
    private final HotlistStore hotlistStore;
    private final BundleWriterPort bundleWriter;
    private final ObjectMapper objectMapper;

    public OpaBundleService(
        OpaBundleProperties properties,
        HotlistStore hotlistStore,
        BundleWriterPort bundleWriter,
        ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.hotlistStore = hotlistStore;
        this.bundleWriter = bundleWriter;
        this.objectMapper = objectMapper;
    }

    // [2] Load persisted hotlist data and build the initial bundle on startup.
    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        hotlistStore.loadFromFile(getDataFilePath(), objectMapper);
        buildBundle();
    }

    // [3] Apply event changes and rebuild the bundle.
    public void handleEvent(HotlistEvent event) {
        hotlistStore.applyEvent(event);
        buildBundle();
    }

    // [4] Resolve current bundle path from configuration.
    public Path getBundlePath() {
        return Paths.get(properties.getOutputDir(), properties.getBundleFileName());
    }

    // [5] Create a bundle if it does not exist yet.
    public synchronized void ensureBundleExists() {
        Path bundlePath = getBundlePath();
        if (!Files.exists(bundlePath)) {
            buildBundle();
        }
    }

    // [6] Generate data.json and bundle archive.
    private void buildBundle() {
        try {
            Path policyDir = Paths.get(properties.getPolicyDir());
            Path dataFilePath = getDataFilePath();
            Path bundlePath = getBundlePath();
            bundleWriter.writeBundle(policyDir, dataFilePath, bundlePath, objectMapper, hotlistStore.snapshotData());
        } catch (Exception e) {
            LOG.error("Failed to build OPA bundle", e);
        }
    }

    // [7] Resolve current data.json path from configuration.
    private Path getDataFilePath() {
        return Paths.get(properties.getOutputDir(), properties.getDataFileName());
    }
}
