package com.barofarm.support.experience.presentation.dto;

import com.barofarm.support.experience.application.dto.ExperienceServiceResponse;
import com.barofarm.support.experience.domain.ExperienceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 체험 프로그램 Response DTO */
@Schema(description = "체험 프로그램 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceResponse {

    @Schema(description = "체험 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID experienceId;

    @Schema(description = "농장 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID farmId;

    @Schema(description = "체험 제목", example = "딸기 수확 체험")
    private String title;

    @Schema(description = "체험 설명", example = "신선한 딸기를 직접 수확해보세요")
    private String description;

    @Schema(description = "1인당 가격 (원)", example = "30000")
    private BigInteger pricePerPerson;

    @Schema(description = "수용 인원", example = "20")
    private Integer capacity;

    @Schema(description = "소요 시간 (분)", example = "120")
    private Integer durationMinutes;

    @Schema(description = "예약 가능 시작일", example = "2025-12-01T12:00:00")
    private LocalDateTime availableStartDate;

    @Schema(description = "예약 가능 종료일", example = "2025-12-31T18:00:00")
    private LocalDateTime availableEndDate;

    @Schema(description = "상태", example = "ON_SALE")
    private ExperienceStatus status;

    @Schema(description = "생성 일시", example = "2025-12-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-12-10T12:00:00")
    private LocalDateTime updatedAt;

    /** ServiceResponse를 Presentation Response로 변환 */
    public static ExperienceResponse from(ExperienceServiceResponse serviceResponse) {
        return new ExperienceResponse(
            serviceResponse.getExperienceId(),
            serviceResponse.getFarmId(),
            serviceResponse.getTitle(),
            serviceResponse.getDescription(),
            serviceResponse.getPricePerPerson(),
            serviceResponse.getCapacity(),
            serviceResponse.getDurationMinutes(),
            serviceResponse.getAvailableStartDate(),
            serviceResponse.getAvailableEndDate(),
            serviceResponse.getStatus(),
            serviceResponse.getCreatedAt(),
            serviceResponse.getUpdatedAt()
        );
    }
}
