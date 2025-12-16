package com.barofarm.support.experience.application;

import com.barofarm.support.common.client.FarmClient;
import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.experience.application.dto.ReservationServiceRequest;
import com.barofarm.support.experience.application.dto.ReservationServiceResponse;
import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import com.barofarm.support.experience.domain.ExperienceStatus;
import com.barofarm.support.experience.domain.Reservation;
import com.barofarm.support.experience.domain.ReservationRepository;
import com.barofarm.support.experience.domain.ReservationStatus;
import com.barofarm.support.experience.exception.ExperienceErrorCode;
import com.barofarm.support.experience.exception.ReservationErrorCode;
import feign.FeignException;
import java.util.UUID;
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
    private final FarmClient farmClient;

    /**
     * 사용자 ID로 농장 ID 조회
     *
     * <p>seller-service에서 404를 반환하면 농장이 없다고 보고 null을 반환한다.
     * 그 외 상태 코드는 그대로 예외를 전파한다.</p>
     *
     * @param userId 사용자 ID
     * @return 농장 ID 또는 null
     */
    private UUID getUserFarmIdOrNull(UUID userId) {
        try {
            return farmClient.getFarmIdByUserId(userId);
        } catch (FeignException e) {
            if (e.status() == 404) {
                return null;
            }
            throw e;
        }
    }

    /**
     * 예약 ID로 조회 (null 체크 및 존재 여부 검증 포함)
     *
     * @param reservationId 예약 ID
     * @return 예약 엔티티
     * @throws CustomException 예약 ID가 null이거나 존재하지 않는 경우
     */
    private Reservation findReservationById(UUID reservationId) {
        if (reservationId == null) {
            throw new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    /**
     * 체험 프로그램 ID로 조회 (null 체크 및 존재 여부 검증 포함)
     *
     * @param experienceId 체험 ID
     * @return 체험 프로그램 엔티티
     * @throws CustomException 체험 ID가 null이거나 존재하지 않는 경우
     */
    private Experience findExperienceById(UUID experienceId) {
        if (experienceId == null) {
            throw new CustomException(ExperienceErrorCode.EXPERIENCE_NOT_FOUND);
        }

        return experienceRepository.findById(experienceId)
                .orElseThrow(() -> new CustomException(ExperienceErrorCode.EXPERIENCE_NOT_FOUND));
    }

    /**
     * 구매자 ID 검증 (buyerId와 userId가 일치하는지 확인)
     *
     * @param buyerId 구매자 ID
     * @param userId 사용자 ID
     * @throws CustomException 권한이 없는 경우
     */
    private void validateBuyerId(UUID buyerId, UUID userId) {
        if (!buyerId.equals(userId)) {
            throw new CustomException(ReservationErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * 구매자 권한 검증
     *
     * @param reservation 예약
     * @param userId 사용자 ID
     * @throws CustomException 권한이 없는 경우
     */
    private void validateBuyerAccess(Reservation reservation, UUID userId) {
        validateBuyerId(reservation.getBuyerId(), userId);
    }

    /**
     * 판매자 권한 검증
     *
     * @param experience 체험 프로그램
     * @param userId 사용자 ID
     * @throws CustomException 권한이 없는 경우
     */
    private void validateSellerAccess(Experience experience, UUID userId) {
        UUID userFarmId = getUserFarmIdOrNull(userId);
        if (userFarmId == null) {
            throw new CustomException(ReservationErrorCode.ACCESS_DENIED);
        }
        if (!experience.getFarmId().equals(userFarmId)) {
            throw new CustomException(ReservationErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * 구매자 또는 판매자 권한 검증 (둘 중 하나만 일치하면 됨)
     *
     * @param reservation 예약
     * @param userId 사용자 ID
     * @return 구매자인지 여부
     * @throws CustomException 둘 다 권한이 없는 경우
     */
    private boolean validateBuyerOrSellerAccess(Reservation reservation, UUID userId) {
        boolean isBuyer = reservation.getBuyerId().equals(userId);

        Experience experience = findExperienceById(reservation.getExperienceId());
        UUID userFarmId = getUserFarmIdOrNull(userId);
        boolean isSeller = userFarmId != null && experience.getFarmId().equals(userFarmId);

        if (!isBuyer && !isSeller) {
            throw new CustomException(ReservationErrorCode.ACCESS_DENIED);
        }

        return isBuyer;
    }

    /**
     * 체험 프로그램 상태 검증
     *
     * @param experience 체험 프로그램
     */
    private void validateExperienceStatus(Experience experience) {
        if (experience.getStatus() != ExperienceStatus.ON_SALE) {
            throw new CustomException(ReservationErrorCode.EXPERIENCE_NOT_AVAILABLE);
        }
    }

    /**
     * 예약 날짜 유효성 검증
     *
     * @param experience 체험 프로그램
     * @param reservedDate 예약 날짜
     */
    private void validateReservationDate(Experience experience, java.time.LocalDate reservedDate) {
        java.time.LocalDate startDate = experience.getAvailableStartDate().toLocalDate();
        java.time.LocalDate endDate = experience.getAvailableEndDate().toLocalDate();

        if (reservedDate.isBefore(startDate) || reservedDate.isAfter(endDate)) {
            throw new CustomException(ReservationErrorCode.INVALID_RESERVATION_DATE);
        }
    }

    /**
     * 삭제 가능한 상태인지 검증
     *
     * @param status 예약 상태
     */
    private void validateDeletableStatus(ReservationStatus status) {
        // REQUESTED나 CANCELED 상태만 삭제 가능
        if (status != ReservationStatus.REQUESTED && status != ReservationStatus.CANCELED) {
            throw new CustomException(ReservationErrorCode.RESERVATION_CANNOT_BE_DELETED);
        }
    }

    /**
     * 상태 변경 가능 여부 검증
     *
     * @param currentStatus 현재 상태
     * @param newStatus 변경하려는 상태
     */
    private void validateStatusTransition(ReservationStatus currentStatus, ReservationStatus newStatus) {
        // 최종 상태는 변경 불가
        if (currentStatus.isFinalStatus()) {
            throw new CustomException(ReservationErrorCode.STATUS_CANNOT_BE_CHANGED);
        }

        // 상태 전환 규칙 검증 (Enum에서 정의된 규칙 사용)
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new CustomException(ReservationErrorCode.INVALID_STATUS_TRANSITION);
        }
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
     * 예약 생성
     *
     * @param userId 사용자 ID
     * @param request 예약 생성 요청
     * @return 생성된 예약
     */
    @Transactional
    public ReservationServiceResponse createReservation(UUID userId, ReservationServiceRequest request) {
        // request.getBuyerId()와 현재 사용자 ID를 비교하여 본인인지 검증
        validateBuyerId(request.getBuyerId(), userId);

        // 체험 프로그램 조회
        Experience experience = findExperienceById(request.getExperienceId());

        // 체험 프로그램 상태 검증
        validateExperienceStatus(experience);

        // 예약 날짜 유효성 검증
        validateReservationDate(experience, request.getReservedDate());

        // 시간대별 capacity 검증
        validateCapacity(
            experience,
            request.getReservedDate(),
            request.getReservedTimeSlot(),
            request.getHeadCount()
        );

        Reservation reservation = request.toEntity();
        reservation.changeStatus(ReservationStatus.REQUESTED);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationServiceResponse.from(savedReservation);
    }

    /**
     * ID로 예약 조회
     *
     * @param userId 사용자 ID
     * @param reservationId 예약 ID
     * @return 예약
     */
    public ReservationServiceResponse getReservationById(UUID userId, UUID reservationId) {
        Reservation reservation = findReservationById(reservationId);

        // 구매자 또는 판매자 권한 검증
        validateBuyerOrSellerAccess(reservation, userId);

        return ReservationServiceResponse.from(reservation);
    }

    /**
     * 체험 ID로 예약 목록 조회 (페이지네이션)
     *
     * @param userId 사용자 ID
     * @param experienceId 체험 ID
     * @param pageable 페이지 정보
     * @return 예약 페이지
     */
    public Page<ReservationServiceResponse> getReservationsByExperienceId(
            UUID userId, UUID experienceId, Pageable pageable) {
        Experience experience = findExperienceById(experienceId);

        // 판매자 권한 검증
        validateSellerAccess(experience, userId);

        Page<Reservation> reservations = reservationRepository.findByExperienceId(experienceId, pageable);
        return reservations.map(ReservationServiceResponse::from);
    }

    /**
     * 구매자 ID로 예약 목록 조회 (페이지네이션)
     *
     * @param userId 사용자 ID
     * @param buyerId 구매자 ID
     * @param pageable 페이지 정보
     * @return 예약 페이지
     */
    public Page<ReservationServiceResponse> getReservationsByBuyerId(UUID userId, UUID buyerId, Pageable pageable) {
        // buyerId와 현재 사용자 ID를 비교
        validateBuyerId(buyerId, userId);

        Page<Reservation> reservations = reservationRepository.findByBuyerId(buyerId, pageable);
        return reservations.map(ReservationServiceResponse::from);
    }

    /**
     * 예약 상태 변경
     *
     * @param userId 사용자 ID
     * @param reservationId 예약 ID
     * @param status 변경할 상태
     * @return 수정된 예약
     */
    @Transactional
    public ReservationServiceResponse updateReservationStatus(
            UUID userId, UUID reservationId, ReservationStatus status) {
        Reservation reservation = findReservationById(reservationId);

        // 상태 변경 가능 여부 검증 (최종 상태 체크를 먼저 수행하여 불필요한 권한 검증 방지)
        validateStatusTransition(reservation.getStatus(), status);

        // 상태에 따라 권한이 다를 수 있음 (구매자는 CANCELED만 가능, 판매자는 CONFIRMED/COMPLETED 가능)
        if (status == ReservationStatus.CANCELED) {
            validateBuyerAccess(reservation, userId);
        } else {
            // CONFIRMED, COMPLETED는 판매자만 가능
            Experience experience = findExperienceById(reservation.getExperienceId());
            validateSellerAccess(experience, userId);
        }

        reservation.changeStatus(status);
        // JPA 더티 체킹, @Transactional 종료 시 자동으로 변경사항이 DB에 반영됨
        return ReservationServiceResponse.from(reservation);
    }

    /**
     * 예약 삭제
     *
     * @param userId 사용자 ID
     * @param reservationId 예약 ID
     */
    @Transactional
    public void deleteReservation(UUID userId, UUID reservationId) {
        Reservation reservation = findReservationById(reservationId);

        // 구매자 권한 검증
        validateBuyerAccess(reservation, userId);

        // 예약 상태 검증
        validateDeletableStatus(reservation.getStatus());

        reservationRepository.deleteById(reservationId);
    }
}
