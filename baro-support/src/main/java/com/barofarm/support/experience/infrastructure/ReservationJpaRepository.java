package com.barofarm.support.experience.infrastructure;

import com.barofarm.support.experience.domain.Reservation;
import com.barofarm.support.experience.domain.ReservationStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 예약 JPA Repository */
public interface ReservationJpaRepository extends JpaRepository<Reservation, UUID> {

    /**
     * 체험 ID로 예약 목록 조회
     *
     * @param experienceId 체험 ID
     * @return 예약 목록
     */
    List<Reservation> findByExperienceId(UUID experienceId);

    /**
     * 구매자 ID로 예약 목록 조회
     *
     * @param buyerId 구매자 ID
     * @return 예약 목록
     */
    List<Reservation> findByBuyerId(UUID buyerId);

    /**
     * 체험 ID로 예약 목록 조회 (페이지네이션)
     *
     * @param experienceId 체험 ID
     * @param pageable 페이지 정보
     * @return 예약 페이지
     */
    Page<Reservation> findByExperienceId(UUID experienceId, Pageable pageable);

    /**
     * 구매자 ID로 예약 목록 조회 (페이지네이션)
     *
     * @param buyerId 구매자 ID
     * @param pageable 페이지 정보
     * @return 예약 페이지
     */
    Page<Reservation> findByBuyerId(UUID buyerId, Pageable pageable);

    /**
     * 특정 체험, 날짜, 시간대의 예약 인원 합계 조회 (확정/요청 상태만)
     *
     * @param experienceId 체험 ID
     * @param reservedDate 예약 날짜
     * @param reservedTimeSlot 예약 시간대
     * @param status1 상태1 (REQUESTED)
     * @param status2 상태2 (CONFIRMED)
     * @return 예약 인원 합계
     */
    @Query("SELECT COALESCE(SUM(r.headCount), 0) FROM Reservation r " +
           "WHERE r.experienceId = :experienceId " +
           "AND r.reservedDate = :reservedDate " +
           "AND r.reservedTimeSlot = :reservedTimeSlot " +
           "AND r.status IN (:status1, :status2)")
    int sumHeadCountByExperienceIdAndReservedDateAndReservedTimeSlot(
        @Param("experienceId") UUID experienceId,
        @Param("reservedDate") LocalDate reservedDate,
        @Param("reservedTimeSlot") String reservedTimeSlot,
        @Param("status1") ReservationStatus status1,
        @Param("status2") ReservationStatus status2
    );
}
