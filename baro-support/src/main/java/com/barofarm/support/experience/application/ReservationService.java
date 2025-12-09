package com.barofarm.support.experience.application;

import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.experience.application.dto.ReservationServiceRequest;
import com.barofarm.support.experience.application.dto.ReservationServiceResponse;
import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import com.barofarm.support.experience.domain.Reservation;
import com.barofarm.support.experience.domain.ReservationRepository;
import com.barofarm.support.experience.domain.ReservationStatus;
import com.barofarm.support.experience.exception.ReservationErrorCode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 예약 애플리케이션 서비스 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ExperienceRepository experienceRepository;

    /**
     * 예약 생성
     *
     * @param request 예약 생성 요청
     * @return 생성된 예약
     */
    @Transactional
    public ReservationServiceResponse createReservation(ReservationServiceRequest request) {
        // 체험 프로그램 조회
        Experience experience = experienceRepository.findById(request.getExperienceId())
            .orElseThrow(() -> new CustomException(ReservationErrorCode.EXPERIENCE_NOT_FOUND));

        // 시간대별 capacity 검증
        validateCapacity(
            experience,
            request.getReservedDate(),
            request.getReservedTimeSlot(),
            request.getHeadCount()
        );

        Reservation reservation = request.toEntity();
        reservation.setStatus(ReservationStatus.REQUESTED);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationServiceResponse.from(savedReservation);
    }

    /**
     * 시간대별 capacity 검증
     *
     * @param experience 체험 프로그램
     * @param reservedDate 예약 날짜
     * @param reservedTimeSlot 예약 시간대
     * @param headCount 예약 인원
     */
    private void validateCapacity(
        Experience experience,
        java.time.LocalDate reservedDate,
        String reservedTimeSlot,
        Integer headCount
    ) {
        // 해당 시간대의 기존 예약 인원 합계 조회 (REQUESTED, CONFIRMED 상태만)
        int existingHeadCount = reservationRepository
            .sumHeadCountByExperienceIdAndReservedDateAndReservedTimeSlot(
                experience.getExperienceId(),
                reservedDate,
                reservedTimeSlot
            );

        // 새로운 예약 인원 + 기존 예약 인원이 capacity를 초과하는지 검증
        if (existingHeadCount + headCount > experience.getCapacity()) {
            throw new CustomException(ReservationErrorCode.CAPACITY_EXCEEDED);
        }
    }

    /**
     * ID로 예약 조회
     *
     * @param reservationId 예약 ID
     * @return 예약
     */
    public ReservationServiceResponse getReservationById(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND));
        return ReservationServiceResponse.from(reservation);
    }

    /**
     * 체험 ID로 예약 목록 조회
     *
     * @param experienceId 체험 ID
     * @return 예약 목록
     */
    public List<ReservationServiceResponse> getReservationsByExperienceId(UUID experienceId) {
        List<Reservation> reservations = reservationRepository.findByExperienceId(experienceId);
        return reservations.stream()
            .map(ReservationServiceResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * 체험 ID로 예약 목록 조회 (페이지네이션)
     *
     * @param experienceId 체험 ID
     * @param pageable 페이지 정보
     * @return 예약 페이지
     */
    public Page<ReservationServiceResponse> getReservationsByExperienceId(UUID experienceId, Pageable pageable) {
        Page<Reservation> reservations = reservationRepository.findByExperienceId(experienceId, pageable);
        return reservations.map(ReservationServiceResponse::from);
    }

    /**
     * 구매자 ID로 예약 목록 조회
     *
     * @param buyerId 구매자 ID
     * @return 예약 목록
     */
    public List<ReservationServiceResponse> getReservationsByBuyerId(UUID buyerId) {
        List<Reservation> reservations = reservationRepository.findByBuyerId(buyerId);
        return reservations.stream()
            .map(ReservationServiceResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * 구매자 ID로 예약 목록 조회 (페이지네이션)
     *
     * @param buyerId 구매자 ID
     * @param pageable 페이지 정보
     * @return 예약 페이지
     */
    public Page<ReservationServiceResponse> getReservationsByBuyerId(UUID buyerId, Pageable pageable) {
        Page<Reservation> reservations = reservationRepository.findByBuyerId(buyerId, pageable);
        return reservations.map(ReservationServiceResponse::from);
    }

    /**
     * 예약 상태 변경
     *
     * @param reservationId 예약 ID
     * @param status 변경할 상태
     * @return 수정된 예약
     */
    @Transactional
    public ReservationServiceResponse updateReservationStatus(UUID reservationId, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        reservation.setStatus(status);
        Reservation updatedReservation = reservationRepository.save(reservation);
        return ReservationServiceResponse.from(updatedReservation);
    }

    /**
     * 예약 삭제
     *
     * @param reservationId 예약 ID
     */
    @Transactional
    public void deleteReservation(UUID reservationId) {
        if (!reservationRepository.existsById(reservationId)) {
            throw new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }
        reservationRepository.deleteById(reservationId);
    }
}
