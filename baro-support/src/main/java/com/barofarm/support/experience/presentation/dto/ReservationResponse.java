package com.barofarm.support.experience.presentation.dto;

import com.barofarm.support.experience.application.dto.ReservationServiceResponse;
import com.barofarm.support.experience.domain.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 예약 응답 DTO */
@Schema(description = "예약 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    @Schema(description = "예약 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID reservationId;

    @Schema(description = "체험 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID experienceId;

    @Schema(description = "구매자 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID buyerId;

    @Schema(description = "방문 날짜", example = "2025-03-15")
    private LocalDate reservedDate;

    @Schema(description = "예약 시간대", example = "10:00-12:00")
    private String reservedTimeSlot;

    @Schema(description = "예약 인원", example = "2")
    private Integer headCount;

    @Schema(description = "총 금액", example = "60000")
    private BigInteger totalPrice;

    @Schema(description = "예약 상태", example = "REQUESTED")
    private ReservationStatus status;

    @Schema(description = "생성 일시", example = "2025-12-09T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-12-09T12:00:00")
    private LocalDateTime updatedAt;

    /** ServiceResponse를 Presentation Response로 변환 */
    public static ReservationResponse from(ReservationServiceResponse serviceResponse) {
        return new ReservationResponse(
            serviceResponse.getReservationId(),
            serviceResponse.getExperienceId(),
            serviceResponse.getBuyerId(),
            serviceResponse.getReservedDate(),
            serviceResponse.getReservedTimeSlot(),
            serviceResponse.getHeadCount(),
            serviceResponse.getTotalPrice(),
            serviceResponse.getStatus(),
            serviceResponse.getCreatedAt(),
            serviceResponse.getUpdatedAt()
        );
    }
}
