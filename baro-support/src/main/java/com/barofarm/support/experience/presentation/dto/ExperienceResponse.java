package com.barofarm.support.experience.presentation.dto;

import com.barofarm.support.experience.application.dto.ExperienceServiceResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 체험 프로그램 응답 DTO */
@Schema(description = "체험 프로그램 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceResponse {

    @Schema(description = "체험 ID", example = "1")
    private Long id;

    @Schema(description = "농장 ID", example = "1")
    private Long farmId;

    @Schema(description = "체험 제목", example = "딸기 수확 체험")
    private String title;

    @Schema(description = "체험 설명", example = "신선한 딸기를 직접 수확해보세요")
    private String description;

    @Schema(description = "가격 (원)", example = "15000")
    private Integer price;

    @Schema(description = "최대 참가자 수", example = "20")
    private Integer maxParticipants;

    @Schema(description = "체험 시작 날짜", example = "2025-03-01")
    private LocalDate startDate;

    @Schema(description = "체험 종료 날짜", example = "2025-05-31")
    private LocalDate endDate;

    @Schema(description = "생성 일시", example = "2025-12-07T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-12-07T12:00:00")
    private LocalDateTime updatedAt;

    /** ServiceResponse를 Presentation Response로 변환 */
    public static ExperienceResponse from(ExperienceServiceResponse serviceResponse) {
        return new ExperienceResponse(serviceResponse.getId(), serviceResponse.getFarmId(), serviceResponse.getTitle(),
                serviceResponse.getDescription(), serviceResponse.getPrice(), serviceResponse.getMaxParticipants(),
                serviceResponse.getStartDate(), serviceResponse.getEndDate(), serviceResponse.getCreatedAt(),
                serviceResponse.getUpdatedAt());
    }
}
