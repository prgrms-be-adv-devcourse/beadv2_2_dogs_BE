package com.barofarm.support.experience.presentation.dto;

import com.barofarm.support.experience.application.dto.ExperienceServiceRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 체험 프로그램 생성/수정 Command DTO */
@Schema(description = "체험 프로그램 생성/수정 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceRequest {

    @Schema(description = "농장 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "농장 ID는 필수입니다")
    private Long farmId;

    @Schema(description = "체험 제목", example = "딸기 수확 체험", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "체험 제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    private String title;

    @Schema(description = "체험 설명", example = "신선한 딸기를 직접 수확해보세요")
    private String description;

    @Schema(description = "가격 (원)", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "가격은 필수입니다")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    private Integer price;

    @Schema(description = "최대 참가자 수", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "최대 참가자 수는 필수입니다")
    @Min(value = 1, message = "최대 참가자 수는 1명 이상이어야 합니다")
    private Integer maxParticipants;

    @Schema(description = "체험 시작 날짜", example = "2025-03-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "시작 날짜는 필수입니다")
    private LocalDate startDate;

    @Schema(description = "체험 종료 날짜", example = "2025-05-31", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "종료 날짜는 필수입니다")
    private LocalDate endDate;

    /** Command DTO를 Service DTO로 변환 */
    public ExperienceServiceRequest toServiceRequest() {
        return new ExperienceServiceRequest(farmId, title, description, price, maxParticipants, startDate, endDate);
    }
}
