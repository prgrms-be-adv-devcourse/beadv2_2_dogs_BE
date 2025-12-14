package com.barofarm.support.experience.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.barofarm.support.experience.exception.ReservationErrorCode;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/** ReservationService 유닛 테스트 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ExperienceRepository experienceRepository;

    @Mock
    private FarmClient farmClient;

    @InjectMocks
    private ReservationService reservationService;

    private UUID experienceId;
    private UUID buyerId;
    private UUID reservationId;
    private UUID farmId;
    private ReservationServiceRequest validRequest;
    private Reservation validReservation;
    private Experience experience;

    @BeforeEach
    void setUp() {
        experienceId = UUID.randomUUID();
        buyerId = UUID.randomUUID();
        reservationId = UUID.randomUUID();
        farmId = UUID.randomUUID();

        validRequest = new ReservationServiceRequest(experienceId, buyerId, LocalDate.of(2025, 3, 15), "10:00-12:00", 2,
                BigInteger.valueOf(30000), null);

        validReservation = new Reservation(reservationId, experienceId, buyerId, LocalDate.of(2025, 3, 15),
                "10:00-12:00", 2, BigInteger.valueOf(30000), ReservationStatus.REQUESTED);

        // Experience는 protected 생성자이므로 Reflection을 사용하거나 Mock을 사용
        // 예약 날짜(2025-03-15)가 체험 가능 기간 내에 있도록 설정
        java.time.LocalDateTime availableStart = java.time.LocalDateTime.of(2025, 3, 1, 9, 0);
        java.time.LocalDateTime availableEnd = java.time.LocalDateTime.of(2025, 3, 31, 18, 0);
        experience = new Experience(experienceId, farmId, "Test Experience", "Description",
                BigInteger.valueOf(15000), 20, 120, availableStart, availableEnd,
                com.barofarm.support.experience.domain.ExperienceStatus.ON_SALE);
    }

    @Test
    @DisplayName("유효한 예약을 생성할 수 있다")
    void createReservation() {
        // given
        when(experienceRepository.findById(experienceId)).thenReturn(Optional.of(experience));
        when(reservationRepository.sumHeadCountByExperienceIdAndReservedDateAndReservedTimeSlot(
                eq(experienceId), any(LocalDate.class), anyString())).thenReturn(0);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(validReservation);

        // when
        ReservationServiceResponse response = reservationService.createReservation(buyerId, validRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getHeadCount()).isEqualTo(2);
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.REQUESTED);
        verify(experienceRepository, times(1)).findById(experienceId);
        verify(reservationRepository, times(1)).sumHeadCountByExperienceIdAndReservedDateAndReservedTimeSlot(
                eq(experienceId), any(LocalDate.class), anyString());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("ID로 예약을 조회할 수 있다")
    void getReservationById() {
        // given
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(validReservation));
        when(experienceRepository.findById(experienceId)).thenReturn(Optional.of(experience));
        // buyerId와 일치하므로 접근 가능 (구매자 권한)

        // when
        ReservationServiceResponse response = reservationService.getReservationById(buyerId, reservationId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getReservationId()).isEqualTo(reservationId);
        assertThat(response.getHeadCount()).isEqualTo(2);
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(experienceRepository, times(1)).findById(experienceId);
    }

    @Test
    @DisplayName("존재하지 않는 예약 ID로 조회하면 예외가 발생한다")
    void getReservationById_NotFound() {
        // given
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.getReservationById(buyerId, reservationId))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException = (CustomException) exception;
                    assertThat(customException.getErrorCode()).isEqualTo(ReservationErrorCode.RESERVATION_NOT_FOUND);
                });
        verify(reservationRepository, times(1)).findById(reservationId);
    }

    @Test
    @DisplayName("체험 ID로 예약 목록을 조회할 수 있다 (페이지네이션)")
    void getReservationsByExperienceId() {
        // given
        UUID sellerId = UUID.randomUUID();
        UUID reservationId2 = UUID.randomUUID();
        Reservation reservation2 = new Reservation(reservationId2, experienceId, buyerId, LocalDate.of(2025, 3, 16),
                "14:00-16:00", 3, BigInteger.valueOf(45000), ReservationStatus.CONFIRMED);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> reservationPage = new PageImpl<>(
                Arrays.asList(validReservation, reservation2), pageable, 2);
        when(experienceRepository.findById(experienceId)).thenReturn(Optional.of(experience));
        when(farmClient.getFarmIdByUserId(sellerId)).thenReturn(farmId);
        when(reservationRepository.findByExperienceId(experienceId, pageable))
                .thenReturn(reservationPage);

        // when
        Page<ReservationServiceResponse> responsePage = reservationService.getReservationsByExperienceId(sellerId, experienceId, pageable);

        // then
        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getContent()).hasSize(2);
        assertThat(responsePage.getContent()).extracting("reservationId").contains(reservationId, reservationId2);
        verify(experienceRepository, times(1)).findById(experienceId);
        verify(farmClient, times(1)).getFarmIdByUserId(sellerId);
        verify(reservationRepository, times(1)).findByExperienceId(experienceId, pageable);
    }

    @Test
    @DisplayName("구매자 ID로 예약 목록을 조회할 수 있다 (페이지네이션)")
    void getReservationsByBuyerId() {
        // given
        UUID otherExperienceId = UUID.randomUUID();
        Reservation reservation2 = new Reservation(UUID.randomUUID(), otherExperienceId, buyerId,
                LocalDate.of(2025, 3, 16), "14:00-16:00", 1, BigInteger.valueOf(15000), ReservationStatus.REQUESTED);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> reservationPage = new PageImpl<>(
                Arrays.asList(validReservation, reservation2), pageable, 2);
        when(reservationRepository.findByBuyerId(buyerId, pageable))
                .thenReturn(reservationPage);

        // when
        Page<ReservationServiceResponse> responsePage = reservationService.getReservationsByBuyerId(buyerId, buyerId, pageable);

        // then
        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getContent()).hasSize(2);
        assertThat(responsePage.getContent()).extracting("buyerId").containsOnly(buyerId);
        verify(reservationRepository, times(1)).findByBuyerId(buyerId, pageable);
    }

    @Test
    @DisplayName("예약 상태를 변경할 수 있다")
    void updateReservationStatus() {
        // given
        UUID sellerId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(validReservation));
        when(experienceRepository.findById(experienceId)).thenReturn(Optional.of(experience));
        when(farmClient.getFarmIdByUserId(sellerId)).thenReturn(farmId);

        // when
        ReservationServiceResponse response = reservationService.updateReservationStatus(sellerId, reservationId,
                ReservationStatus.CONFIRMED);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(experienceRepository, times(1)).findById(experienceId);
        verify(farmClient, times(1)).getFarmIdByUserId(sellerId);
        // JPA 더티 체킹 사용하므로 save 호출하지 않음
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("존재하지 않는 예약 상태를 변경하려고 하면 예외가 발생한다")
    void updateReservationStatus_NotFound() {
        // given
        UUID sellerId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservationStatus(sellerId, reservationId, ReservationStatus.CONFIRMED))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException = (CustomException) exception;
                    assertThat(customException.getErrorCode()).isEqualTo(ReservationErrorCode.RESERVATION_NOT_FOUND);
                });
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다")
    void deleteReservation() {
        // given
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(validReservation));
        doNothing().when(reservationRepository).deleteById(reservationId);

        // when
        reservationService.deleteReservation(buyerId, reservationId);

        // then
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(reservationRepository, times(1)).deleteById(reservationId);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제하려고 하면 예외가 발생한다")
    void deleteReservation_NotFound() {
        // given
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.deleteReservation(buyerId, reservationId))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException = (CustomException) exception;
                    assertThat(customException.getErrorCode()).isEqualTo(ReservationErrorCode.RESERVATION_NOT_FOUND);
                });
        verify(reservationRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("체험이 ON_SALE 상태가 아니면 예약 생성 시 예외가 발생한다")
    void createReservation_ExperienceNotAvailable() {
        // given
        Experience closedExperience = new Experience(experienceId, UUID.randomUUID(), "Test Experience", "Description",
                BigInteger.valueOf(15000), 20, 120, java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusDays(30),
                ExperienceStatus.CLOSED);
        when(experienceRepository.findById(experienceId)).thenReturn(Optional.of(closedExperience));

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(buyerId, validRequest))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException = (CustomException) exception;
                    assertThat(customException.getErrorCode()).isEqualTo(ReservationErrorCode.EXPERIENCE_NOT_AVAILABLE);
                });
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약 날짜가 체험 가능 기간 밖이면 예외가 발생한다")
    void createReservation_InvalidDate() {
        // given
        java.time.LocalDate invalidDate = LocalDate.of(2025, 6, 1); // 체험 가능 기간 밖
        ReservationServiceRequest invalidDateRequest = new ReservationServiceRequest(experienceId, buyerId, invalidDate,
                "10:00-12:00", 2, BigInteger.valueOf(30000), null);
        when(experienceRepository.findById(experienceId)).thenReturn(Optional.of(experience));

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(buyerId, invalidDateRequest))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException = (CustomException) exception;
                    assertThat(customException.getErrorCode()).isEqualTo(ReservationErrorCode.INVALID_RESERVATION_DATE);
                });
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("수용 인원을 초과하면 예약 생성 시 예외가 발생한다")
    void createReservation_CapacityExceeded() {
        // given
        when(experienceRepository.findById(experienceId)).thenReturn(Optional.of(experience));
        // 기존 예약 인원이 19명 (capacity 20 - 요청 인원 2 = 18이어야 하는데 19명이 이미 예약됨)
        when(reservationRepository.sumHeadCountByExperienceIdAndReservedDateAndReservedTimeSlot(
                eq(experienceId), any(LocalDate.class), anyString())).thenReturn(19);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(buyerId, validRequest))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException = (CustomException) exception;
                    assertThat(customException.getErrorCode()).isEqualTo(ReservationErrorCode.CAPACITY_EXCEEDED);
                });
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("잘못된 상태 전환 시 예외가 발생한다")
    void updateReservationStatus_InvalidTransition() {
        // given
        UUID sellerId = UUID.randomUUID();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(validReservation));
        // REQUESTED -> COMPLETED는 불가능 (CONFIRMED를 거쳐야 함)
        // 상태 검증이 먼저 수행되므로 experienceRepository와 farmClient stubbing 불필요

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservationStatus(sellerId, reservationId, ReservationStatus.COMPLETED))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException = (CustomException) exception;
                    assertThat(customException.getErrorCode()).isEqualTo(ReservationErrorCode.INVALID_STATUS_TRANSITION);
                });
    }

    @Test
    @DisplayName("CANCELED 상태의 예약은 상태 변경이 불가능하다")
    void updateReservationStatus_CanceledCannotBeChanged() {
        // given
        UUID sellerId = UUID.randomUUID();
        Reservation canceledReservation = new Reservation(reservationId, experienceId, buyerId, LocalDate.of(2025, 3, 15),
                "10:00-12:00", 2, BigInteger.valueOf(30000), ReservationStatus.CANCELED);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(canceledReservation));
        // 상태 검증이 먼저 수행되므로 experienceRepository와 farmClient stubbing 불필요

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservationStatus(sellerId, reservationId, ReservationStatus.CONFIRMED))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException = (CustomException) exception;
                    assertThat(customException.getErrorCode()).isEqualTo(ReservationErrorCode.STATUS_CANNOT_BE_CHANGED);
                });
    }

    @Test
    @DisplayName("COMPLETED 상태의 예약은 상태 변경이 불가능하다")
    void updateReservationStatus_CompletedCannotBeChanged() {
        // given
        // CANCELED는 구매자만 가능하므로 buyerId 사용
        Reservation completedReservation = new Reservation(reservationId, experienceId, buyerId, LocalDate.of(2025, 3, 15),
                "10:00-12:00", 2, BigInteger.valueOf(30000), ReservationStatus.COMPLETED);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(completedReservation));

        // when & then
        // COMPLETED 상태는 최종 상태이므로 상태 변경 불가 (권한 검증 전에 상태 검증이 이루어짐)
        assertThatThrownBy(() -> reservationService.updateReservationStatus(buyerId, reservationId, ReservationStatus.CANCELED))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException = (CustomException) exception;
                    assertThat(customException.getErrorCode()).isEqualTo(ReservationErrorCode.STATUS_CANNOT_BE_CHANGED);
                });
    }

    @Test
    @DisplayName("CONFIRMED나 COMPLETED 상태의 예약은 삭제할 수 없다")
    void deleteReservation_CannotDeleteConfirmedOrCompleted() {
        // given
        Reservation confirmedReservation = new Reservation(reservationId, experienceId, buyerId, LocalDate.of(2025, 3, 15),
                "10:00-12:00", 2, BigInteger.valueOf(30000), ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(confirmedReservation));

        // when & then
        assertThatThrownBy(() -> reservationService.deleteReservation(buyerId, reservationId))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException = (CustomException) exception;
                    assertThat(customException.getErrorCode()).isEqualTo(ReservationErrorCode.RESERVATION_CANNOT_BE_DELETED);
                });
        verify(reservationRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("REQUESTED 상태의 예약은 삭제할 수 있다")
    void deleteReservation_CanDeleteRequested() {
        // given
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(validReservation));
        doNothing().when(reservationRepository).deleteById(reservationId);

        // when
        reservationService.deleteReservation(buyerId, reservationId);

        // then
        verify(reservationRepository, times(1)).deleteById(reservationId);
    }

    @Test
    @DisplayName("CANCELED 상태의 예약은 삭제할 수 있다")
    void deleteReservation_CanDeleteCanceled() {
        // given
        Reservation canceledReservation = new Reservation(reservationId, experienceId, buyerId, LocalDate.of(2025, 3, 15),
                "10:00-12:00", 2, BigInteger.valueOf(30000), ReservationStatus.CANCELED);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(canceledReservation));
        doNothing().when(reservationRepository).deleteById(reservationId);

        // when
        reservationService.deleteReservation(buyerId, reservationId);

        // then
        verify(reservationRepository, times(1)).deleteById(reservationId);
    }
}
