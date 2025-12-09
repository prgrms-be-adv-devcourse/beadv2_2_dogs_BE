package com.barofarm.support.experience.domain;

import com.barofarm.support.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 체험 예약 엔티티 (RESERVATION) */
@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reservation_id", columnDefinition = "BINARY(16)")
    private UUID reservationId;

    @Column(name = "experience_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID experienceId;

    @Column(name = "buyer_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID buyerId;

    @Column(name = "reserved_date", nullable = false, columnDefinition = "DATE")
    private LocalDate reservedDate;

    @Column(name = "reserved_time_slot", nullable = false, length = 50)
    private String reservedTimeSlot;

    @Column(name = "head_count", nullable = false)
    private Integer headCount;

    @Column(name = "total_price", nullable = false)
    private BigInteger totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;
}
