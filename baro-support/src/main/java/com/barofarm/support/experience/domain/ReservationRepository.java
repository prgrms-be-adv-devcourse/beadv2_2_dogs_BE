package com.barofarm.support.experience.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 예약 리포지토리 인터페이스 (Port) */
public interface ReservationRepository {

    /**
     * 예약 저장
     *
     * @param reservation 저장할 예약
     * @return 저장된 예약
     */
    Reservation save(Reservation reservation);

    /**
     * ID로 예약 조회
     *
     * @param reservationId 예약 ID
     * @return 예약 (Optional)
     */
    Optional<Reservation> findById(UUID reservationId);

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
     * ID로 예약 존재 여부 확인
     *
     * @param reservationId 예약 ID
     * @return 존재 여부
     */
    boolean existsById(UUID reservationId);

    /**
     * 예약 삭제
     *
     * @param reservationId 예약 ID
     */
    void deleteById(UUID reservationId);

    /**
     * 특정 체험, 날짜, 시간대의 예약 인원 합계 조회 (확정/요청 상태만)
     *
     * @param experienceId 체험 ID
     * @param reservedDate 예약 날짜
     * @param reservedTimeSlot 예약 시간대
     * @return 예약 인원 합계
     */
    int sumHeadCountByExperienceIdAndReservedDateAndReservedTimeSlot(
        UUID experienceId,
        LocalDate reservedDate,
        String reservedTimeSlot
    );

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
}
