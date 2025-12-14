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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** 체험 예약 엔티티 (RESERVATION) */
@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
// Set/Map 사용 시 동일성 비교를 위해 ID만 사용 (모든 필드 포함 시 영속성 컨텍스트 이슈 발생 가능)
@EqualsAndHashCode(callSuper = false, of = "reservationId")
@ToString(callSuper = false)
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

    /**
     * 예약 정보 업데이트
     *
     * @param reservedDate 예약 날짜
     * @param reservedTimeSlot 예약 시간대
     * @param headCount 예약 인원
     * @param totalPrice 총 가격
     */
    public void update(
            LocalDate reservedDate,
            String reservedTimeSlot,
            Integer headCount,
            BigInteger totalPrice
    ) {
        if (reservedDate != null) {
            this.reservedDate = reservedDate;
        }
        if (reservedTimeSlot != null) {
            this.reservedTimeSlot = reservedTimeSlot;
        }
        if (headCount != null) {
            this.headCount = headCount;
        }
        if (totalPrice != null) {
            this.totalPrice = totalPrice;
        }
    }

    /**
     * 예약 상태 변경
     *
     * @param status 변경할 예약 상태
     */
    public void changeStatus(ReservationStatus status) {
        if (status != null) {
            this.status = status;
        }
    }
}
