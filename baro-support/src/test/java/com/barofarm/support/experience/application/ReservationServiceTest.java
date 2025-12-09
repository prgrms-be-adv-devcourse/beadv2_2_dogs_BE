package com.barofarm.support.experience.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.experience.application.dto.ReservationServiceRequest;
import com.barofarm.support.experience.application.dto.ReservationServiceResponse;
import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import com.barofarm.support.experience.domain.Reservation;
import com.barofarm.support.experience.domain.ReservationRepository;
import com.barofarm.support.experience.domain.ReservationStatus;
import com.barofarm.support.experience.exception.ReservationErrorCode;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** ReservationService 유닛 테스트 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ExperienceRepository experienceRepository;

    @InjectMocks
    private ReservationService reservationService;

    private UUID experienceId;
    private UUID buyerId;
    private UUID reservationId;
    private ReservationServiceRequest validRequest;
    private Reservation validReservation;
    private Experience experience;

    @BeforeEach
    void setUp() {
        experienceId = UUID.randomUUID();
        buyerId = UUID.randomUUID();
        reservationId = UUID.randomUUID();

        validRequest = new ReservationServiceRequest(experienceId, buyerId, LocalDate.of(2025, 3, 15), "10:00-12:00", 2,
                BigInteger.valueOf(30000), null);

        validReservation = new Reservation(reservationId, experienceId, buyerId, LocalDate.of(2025, 3, 15),
                "10:00-12:00", 2, BigInteger.valueOf(30000), ReservationStatus.REQUESTED);

        // Experience는 protected 생성자이므로 Reflection을 사용하거나 Mock을 사용
        experience = new Experience(experienceId, UUID.randomUUID(), "Test Experience", "Description",
                BigInteger.valueOf(15000), 20, 120, java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusDays(30),
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
        ReservationServiceResponse response = reservationService.createReservation(validRequest);

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

        // when
        ReservationServiceResponse response = reservationService.getReservationById(reservationId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getReservationId()).isEqualTo(reservationId);
        assertThat(response.getHeadCount()).isEqualTo(2);
        verify(reservationRepository, times(1)).findById(reservationId);
    }

    @Test
    @DisplayName("존재하지 않는 예약 ID로 조회하면 예외가 발생한다")
    void getReservationById_NotFound() {
        // given
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.getReservationById(reservationId))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException = (CustomException) exception;
                    assertThat(customException.getErrorCode()).isEqualTo(ReservationErrorCode.RESERVATION_NOT_FOUND);
                });
        verify(reservationRepository, times(1)).findById(reservationId);
    }

    @Test
    @DisplayName("체험 ID로 예약 목록을 조회할 수 있다")
    void getReservationsByExperienceId() {
        // given
        UUID reservationId2 = UUID.randomUUID();
        Reservation reservation2 = new Reservation(reservationId2, experienceId, buyerId, LocalDate.of(2025, 3, 16),
                "14:00-16:00", 3, BigInteger.valueOf(45000), ReservationStatus.CONFIRMED);

        when(reservationRepository.findByExperienceId(experienceId))
                .thenReturn(Arrays.asList(validReservation, reservation2));

        // when
        List<ReservationServiceResponse> responses = reservationService.getReservationsByExperienceId(experienceId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("reservationId").contains(reservationId, reservationId2);
        verify(reservationRepository, times(1)).findByExperienceId(experienceId);
    }

    @Test
    @DisplayName("구매자 ID로 예약 목록을 조회할 수 있다")
    void getReservationsByBuyerId() {
        // given
        UUID otherExperienceId = UUID.randomUUID();
        Reservation reservation2 = new Reservation(UUID.randomUUID(), otherExperienceId, buyerId,
                LocalDate.of(2025, 3, 16), "14:00-16:00", 1, BigInteger.valueOf(15000), ReservationStatus.REQUESTED);

        when(reservationRepository.findByBuyerId(buyerId)).thenReturn(Arrays.asList(validReservation, reservation2));

        // when
        List<ReservationServiceResponse> responses = reservationService.getReservationsByBuyerId(buyerId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("buyerId").containsOnly(buyerId);
        verify(reservationRepository, times(1)).findByBuyerId(buyerId);
    }

    @Test
    @DisplayName("예약 상태를 변경할 수 있다")
    void updateReservationStatus() {
        // given
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(validReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(validReservation);

        // when
        ReservationServiceResponse response = reservationService.updateReservationStatus(reservationId,
                ReservationStatus.CONFIRMED);

        // then
        assertThat(response).isNotNull();
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("존재하지 않는 예약 상태를 변경하려고 하면 예외가 발생한다")
    void updateReservationStatus_NotFound() {
        // given
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservationStatus(reservationId, ReservationStatus.CONFIRMED))
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
        when(reservationRepository.existsById(reservationId)).thenReturn(true);
        doNothing().when(reservationRepository).deleteById(reservationId);

        // when
        reservationService.deleteReservation(reservationId);

        // then
        verify(reservationRepository, times(1)).existsById(reservationId);
        verify(reservationRepository, times(1)).deleteById(reservationId);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제하려고 하면 예외가 발생한다")
    void deleteReservation_NotFound() {
        // given
        when(reservationRepository.existsById(reservationId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> reservationService.deleteReservation(reservationId))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException = (CustomException) exception;
                    assertThat(customException.getErrorCode()).isEqualTo(ReservationErrorCode.RESERVATION_NOT_FOUND);
                });
        verify(reservationRepository, never()).deleteById(any());
    }
}
