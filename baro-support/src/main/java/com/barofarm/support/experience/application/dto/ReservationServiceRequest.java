package com.barofarm.support.experience.application.dto;

import com.barofarm.support.experience.domain.Reservation;
import com.barofarm.support.experience.domain.ReservationStatus;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 예약 서비스 Request DTO */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationServiceRequest {

    private UUID experienceId;
    private UUID buyerId;
    private LocalDate reservedDate;
    private String reservedTimeSlot;
    private Integer headCount;
    private BigInteger totalPrice;
    private ReservationStatus status;

    /** DTO를 엔티티로 변환 */
    public Reservation toEntity() {
        // status는 서비스 레이어에서 설정하므로 null로 전달
        return new Reservation(null, experienceId, buyerId, reservedDate, reservedTimeSlot,
            headCount, totalPrice, null);
    }
}
