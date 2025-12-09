package com.barofarm.support.experience.application.dto;

import com.barofarm.support.experience.domain.Reservation;
import com.barofarm.support.experience.domain.ReservationStatus;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 예약 서비스 레이어 응답 DTO */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationServiceResponse {

    private UUID reservationId;
    private UUID experienceId;
    private UUID buyerId;
    private LocalDate reservedDate;
    private String reservedTimeSlot;
    private Integer headCount;
    private BigInteger totalPrice;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Reservation 엔티티를 ReservationServiceResponse로 변환 */
    public static ReservationServiceResponse from(Reservation reservation) {
        return new ReservationServiceResponse(
            reservation.getReservationId(),
            reservation.getExperienceId(),
            reservation.getBuyerId(),
            reservation.getReservedDate(),
            reservation.getReservedTimeSlot(),
            reservation.getHeadCount(),
            reservation.getTotalPrice(),
            reservation.getStatus(),
            reservation.getCreatedAt(),
            reservation.getUpdatedAt()
        );
    }
}
