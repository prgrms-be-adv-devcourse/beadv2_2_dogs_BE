package com.barofarm.support.experience.presentation.dto;

import com.barofarm.support.experience.application.dto.ReservationServiceRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 예약 생성/수정 Command DTO */
@Schema(description = "예약 생성/수정 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {

    @Schema(description = "체험 ID", example = "550e8400-e29b-41d4-a716-446655440000",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "체험 ID는 필수입니다")
    private UUID experienceId;

    @Schema(description = "구매자 ID", example = "550e8400-e29b-41d4-a716-446655440001",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "구매자 ID는 필수입니다")
    private UUID buyerId;

    @Schema(description = "방문 날짜", example = "2025-12-15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "방문 날짜는 필수입니다")
    private LocalDate reservedDate;

    @Schema(description = "예약 시간대", example = "10:00-12:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "예약 시간대는 필수입니다")
    private String reservedTimeSlot;

    @Schema(description = "예약 인원", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "예약 인원은 필수입니다")
    @Min(value = 1, message = "예약 인원은 1명 이상이어야 합니다")
    private Integer headCount;

    @Schema(description = "총 금액", example = "60000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "총 금액은 필수입니다")
    @Min(value = 0, message = "총 금액은 0원 이상이어야 합니다")
    private BigInteger totalPrice;

    /** Command DTO를 Service DTO로 변환 */
    public ReservationServiceRequest toServiceRequest() {
        return new ReservationServiceRequest(experienceId, buyerId, reservedDate, reservedTimeSlot, headCount,
            totalPrice, null);
    }
}
