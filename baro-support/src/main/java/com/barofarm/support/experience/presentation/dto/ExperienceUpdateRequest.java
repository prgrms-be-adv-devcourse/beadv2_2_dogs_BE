package com.barofarm.support.experience.presentation.dto;

import com.barofarm.support.experience.application.dto.ExperienceServiceRequest;
import com.barofarm.support.experience.domain.ExperienceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.math.BigInteger;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 체험 프로그램 수정 Command DTO (부분 업데이트 지원) */
@Schema(description = "체험 프로그램 수정 요청 (부분 업데이트 가능)")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceUpdateRequest {

    @Schema(description = "체험 제목", example = "딸기 수확 체험", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String title;

    @Schema(description = "체험 설명", example = "신선한 딸기를 직접 수확해보세요", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(description = "1인당 가격 (원)", example = "30000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    private BigInteger pricePerPerson;

    @Schema(description = "수용 인원", example = "20", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Min(value = 1, message = "수용 인원은 1명 이상이어야 합니다")
    private Integer capacity;

    @Schema(description = "소요 시간 (분)", example = "120", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Min(value = 1, message = "소요 시간은 1분 이상이어야 합니다")
    private Integer durationMinutes;

    @Schema(description = "예약 가능 시작일", example = "2025-12-01T09:00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime availableStartDate;

    @Schema(description = "예약 가능 종료일", example = "2025-12-31T18:00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime availableEndDate;

    @Schema(description = "상태", example = "ON_SALE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ExperienceStatus status;

    /** Command DTO를 Service DTO로 변환 (null 필드는 제외) */
    public ExperienceServiceRequest toServiceRequest() {
        return new ExperienceServiceRequest(
            null, // farmId는 수정 시 변경 불가
            title,
            description,
            pricePerPerson,
            capacity,
            durationMinutes,
            availableStartDate,
            availableEndDate,
            status
        );
    }
}
