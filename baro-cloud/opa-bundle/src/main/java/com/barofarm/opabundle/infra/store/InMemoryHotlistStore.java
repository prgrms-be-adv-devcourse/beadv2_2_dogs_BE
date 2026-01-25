package com.barofarm.opabundle.infra.store;

import com.barofarm.opabundle.application.dto.HotlistEvent;
import com.barofarm.opabundle.application.port.HotlistStore;
import com.barofarm.opabundle.domain.HotlistEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// 이벤트를 적용해서 hotlist의 현재 상태를 메모리에 유지하고, data.json으로 내보낼 스냅샷을 제공하는 역할
// 중복 이벤트를 eventId로 걸러주는 간단한 Idempotency(멱등) 캐시도 포함

@Component
// [0] Hotlist 스냅샷을 메모리에 유지하고 data.json용 데이터를 제공하는 저장소 클래스.
public class InMemoryHotlistStore implements HotlistStore {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryHotlistStore.class);
    private static final int MAX_EVENT_IDS = 10000;

    // [1] In-memory hotlist snapshot keyed by subjectType -> subjectId.
    private final Map<String, Map<String, HotlistEntry>> subjects = new ConcurrentHashMap<>();
    // [1-1] Best-effort idempotency cache for hotlist events.
    private final Map<String, Boolean> processedEventIds = new LinkedHashMap<>();

    // [2] Apply an incoming event to the in-memory hotlist.
    public synchronized void applyEvent(HotlistEvent event) {
        if (event == null) {
            return;
        }
        if (isDuplicateEvent(event)) {
            return;
        }
        String subjectType = normalizeSubjectType(event.getSubjectType());
        String subjectId = safeTrim(event.getSubjectId());
        if (subjectType == null || subjectId == null) {
            LOG.warn("Ignoring hotlist event with missing subjectType/subjectId");
            return;
        }

        boolean active = event.getActive() == null || event.getActive();
        if (!active) {
            Map<String, HotlistEntry> entries = subjects.get(subjectType);
            if (entries != null) {
                entries.remove(subjectId);
                if (entries.isEmpty()) {
                    subjects.remove(subjectType);
                }
            }
            return;
        }

        Map<String, HotlistEntry> entries =
            subjects.computeIfAbsent(subjectType, ignored -> new ConcurrentHashMap<>());

        List<String> flags = event.getFlags() == null ? Collections.emptyList() : event.getFlags();
        String updatedAt = event.getUpdatedAt() == null ? Instant.now().toString() : event.getUpdatedAt();
        HotlistEntry entry = new HotlistEntry(true, flags, event.getReason(), updatedAt);
        String status = normalizeStatus(event.getStatus(), active);
        if (active && status == null) {
            LOG.warn("Hotlist event missing status for {}:{}", subjectType, subjectId);
            status = "BLOCKED";
        }
        entry.setStatus(status);
        entries.put(subjectId, entry);
    }

    // [3] Produce a data.json-compatible snapshot for OPA.
    public synchronized Map<String, Object> snapshotData() {
        Map<String, Object> hotlist = new HashMap<>();
        hotlist.put("users", copyEntries(subjects.get("users")));
        hotlist.put("sellers", copyEntries(subjects.get("sellers")));

        Map<String, Object> root = new HashMap<>();
        root.put("hotlist", hotlist);
        return root;
    }

    // [4] Load hotlist state from a persisted data.json file on startup.
    public synchronized void loadFromFile(Path dataFilePath, ObjectMapper objectMapper) {
        if (dataFilePath == null || !Files.exists(dataFilePath)) {
            return;
        }
        try {
            String content = Files.readString(dataFilePath, StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(content);
            subjects.clear();
            JsonNode hotlistNode = root.path("hotlist");
            loadDirectType(hotlistNode.path("users"), "users", objectMapper);
            loadDirectType(hotlistNode.path("sellers"), "sellers", objectMapper);
            loadLegacySubjects(hotlistNode.path("subjects"), objectMapper);
        } catch (IOException e) {
            LOG.warn("Failed to load hotlist data from {}", dataFilePath, e);
        }
    }

    // [5] Normalize and validate string keys.
    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeStatus(String status, boolean active) {
        String trimmed = safeTrim(status);
        if (!active) {
            return null;
        }
        return trimmed;
    }

    private boolean isDuplicateEvent(HotlistEvent event) {
        String eventId = safeTrim(event.getEventId());
        if (eventId == null) {
            return false;
        }
        if (processedEventIds.containsKey(eventId)) {
            return true;
        }
        processedEventIds.put(eventId, Boolean.TRUE);
        evictOldEventIds();
        return false;
    }

    private void evictOldEventIds() {
        if (processedEventIds.size() <= MAX_EVENT_IDS) {
            return;
        }
        Iterator<String> iterator = processedEventIds.keySet().iterator();
        while (processedEventIds.size() > MAX_EVENT_IDS && iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    private String normalizeSubjectType(String value) {
        String trimmed = safeTrim(value);
        if (trimmed == null) {
            return null;
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        if ("user".equals(normalized) || "users".equals(normalized)) {
            return "users";
        }
        if ("seller".equals(normalized) || "sellers".equals(normalized)) {
            return "sellers";
        }
        return normalized;
    }

    private Map<String, HotlistEntry> copyEntries(Map<String, HotlistEntry> source) {
        if (source == null || source.isEmpty()) {
            return new HashMap<>();
        }
        return new HashMap<>(source);
    }

    private void loadDirectType(JsonNode node, String subjectType, ObjectMapper objectMapper) {
        if (!node.isObject()) {
            return;
        }
        Map<String, HotlistEntry> typeMap = new ConcurrentHashMap<>();
        node.fields().forEachRemaining(idEntry -> {
            try {
                HotlistEntry hotlistEntry = objectMapper.treeToValue(idEntry.getValue(), HotlistEntry.class);
                typeMap.put(idEntry.getKey(), hotlistEntry);
            } catch (IOException e) {
                LOG.warn("Failed to parse hotlist entry for {}:{}", subjectType, idEntry.getKey(), e);
            }
        });
        if (!typeMap.isEmpty()) {
            subjects.put(subjectType, typeMap);
        }
    }

    private void loadLegacySubjects(JsonNode subjectsNode, ObjectMapper objectMapper) {
        if (!subjectsNode.isObject()) {
            return;
        }
        subjectsNode.fields().forEachRemaining(typeEntry -> {
            String subjectType = normalizeSubjectType(typeEntry.getKey());
            if (subjectType == null) {
                return;
            }
            JsonNode subjectNode = typeEntry.getValue();
            if (!subjectNode.isObject()) {
                return;
            }
            Map<String, HotlistEntry> typeMap =
                subjects.computeIfAbsent(subjectType, ignored -> new ConcurrentHashMap<>());
            subjectNode.fields().forEachRemaining(idEntry -> {
                try {
                    HotlistEntry hotlistEntry = objectMapper.treeToValue(idEntry.getValue(), HotlistEntry.class);
                    typeMap.putIfAbsent(idEntry.getKey(), hotlistEntry);
                } catch (IOException e) {
                    LOG.warn("Failed to parse hotlist entry for {}:{}", subjectType, idEntry.getKey(), e);
                }
            });
        });
    }
}
