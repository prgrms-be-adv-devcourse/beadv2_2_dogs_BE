package com.barofarm.support.experience.presentation.dto;

import com.barofarm.support.experience.application.dto.ExperienceServiceRequest;
import com.barofarm.support.experience.domain.ExperienceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 체험 프로그램 생성 Command DTO */
@Schema(description = "체험 프로그램 생성 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceCreateRequest {

    @Schema(description = "농장 ID", example = "550e8400-e29b-41d4-a716-446655440000",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "농장 ID는 필수입니다")
    private UUID farmId;

    @Schema(description = "체험 제목", example = "딸기 수확 체험", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "체험 제목은 필수입니다")
    private String title;

    @Schema(description = "체험 설명", example = "신선한 딸기를 직접 수확해보세요")
    private String description;

    @Schema(description = "1인당 가격 (원)", example = "30000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "1인당 가격은 필수입니다")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    private BigInteger pricePerPerson;

    @Schema(description = "수용 인원", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "수용 인원은 필수입니다")
    @Min(value = 1, message = "수용 인원은 1명 이상이어야 합니다")
    private Integer capacity;

    @Schema(description = "소요 시간 (분)", example = "120", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "소요 시간은 필수입니다")
    @Min(value = 1, message = "소요 시간은 1분 이상이어야 합니다")
    private Integer durationMinutes;

    @Schema(description = "예약 가능 시작일", example = "2025-12-01T09:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "예약 가능 시작일은 필수입니다")
    private LocalDateTime availableStartDate;

    @Schema(description = "예약 가능 종료일", example = "2025-12-31T18:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "예약 가능 종료일은 필수입니다")
    private LocalDateTime availableEndDate;

    @Schema(description = "상태", example = "ON_SALE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "상태는 필수입니다")
    private ExperienceStatus status;

    /** Command DTO를 Service DTO로 변환 */
    public ExperienceServiceRequest toServiceRequest() {
        return new ExperienceServiceRequest(farmId, title, description, pricePerPerson, capacity, durationMinutes,
            availableStartDate, availableEndDate, status);
    }
}

