package com.barofarm.ai.ranking.application;

import com.barofarm.ai.ranking.dto.ProductLogData;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 로그 파일 읽기 및 파싱 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogFileReaderService {

    private static final String LOG_BASE_PATH = "/mnt/s3/logs/events";
    private static final String CLICK_LOG_PREFIX = "product_detail_click";
    private static final String DWELL_TIME_LOG_PREFIX = "product_detail_dwell_time";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");

    private final ObjectMapper objectMapper;

    @Value("${ranking.log.base-path:" + LOG_BASE_PATH + "}")
    private String logBasePath;

    /**
     * 현재 시간 기준 1시간 전 로그 파일들을 읽어서 파싱
     *
     * @return 클릭 로그와 체류 시간 로그 데이터 리스트
     */
    public LogFileData readPreviousHourLogs() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime previousHour = now.minusHours(1);

        String date = previousHour.format(DATE_FORMATTER);
        int hour = previousHour.getHour();

        log.info("읽을 로그 파일: {}일 {}시", date, hour);

        List<ProductLogData> clickLogs = readClickLogs(date, hour);
        List<ProductLogData> dwellTimeLogs = readDwellTimeLogs(date, hour);

        return new LogFileData(clickLogs, dwellTimeLogs, date, hour);
    }

    /**
     * 클릭 로그 파일 읽기
     */
    private List<ProductLogData> readClickLogs(String date, int hour) {
        String fileName = String.format("%s-%s-%02d.log", CLICK_LOG_PREFIX, date, hour);
        Path filePath = Paths.get(logBasePath, fileName);

        return readLogFile(filePath);
    }

    /**
     * 체류 시간 로그 파일 읽기
     */
    private List<ProductLogData> readDwellTimeLogs(String date, int hour) {
        String fileName = String.format("%s-%s-%02d.log", DWELL_TIME_LOG_PREFIX, date, hour);
        Path filePath = Paths.get(logBasePath, fileName);

        return readLogFile(filePath);
    }

    /**
     * 로그 파일을 읽어서 JSON 파싱
     */
    private List<ProductLogData> readLogFile(Path filePath) {
        List<ProductLogData> logs = new ArrayList<>();

        if (!Files.exists(filePath)) {
            log.warn("로그 파일이 존재하지 않습니다: {}", filePath);
            return logs;
        }

        try (Stream<String> lines = Files.lines(filePath)) {
            lines.forEach(line -> {
                try {
                    if (line.trim().isEmpty()) {
                        return; // 빈 라인 스킵
                    }
                    ProductLogData logData = objectMapper.readValue(line, ProductLogData.class);
                    logs.add(logData);
                } catch (Exception e) {
                    log.warn("로그 라인 파싱 실패 (스킵): {} - {}", line, e.getMessage());
                }
            });
        } catch (IOException e) {
            log.error("로그 파일 읽기 실패: {}", filePath, e);
        }

        log.info("로그 파일 읽기 완료: {} ({}개 라인)", filePath, logs.size());
        return logs;
    }

    /**
     * 로그 파일 데이터를 담는 내부 클래스
     */
    public record LogFileData(
        List<ProductLogData> clickLogs,
        List<ProductLogData> dwellTimeLogs,
        String date,
        int hour
    ) {
    }
}
