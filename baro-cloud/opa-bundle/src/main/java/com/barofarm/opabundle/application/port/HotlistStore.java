package com.barofarm.opabundle.application.port;

import com.barofarm.opabundle.application.dto.HotlistEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.Map;

// [0] Hotlist 상태 저장소의 추상 계약을 정의하는 포트 인터페이스.
public interface HotlistStore {

    // [1] Apply an incoming hotlist event to the current state.
    void applyEvent(HotlistEvent event);

    // [2] Provide a snapshot suitable for data.json.
    Map<String, Object> snapshotData();

    // [3] Restore state from the persisted data.json on startup.
    void loadFromFile(Path dataFilePath, ObjectMapper objectMapper);
}
