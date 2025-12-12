package com.barofarm.support.experience.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.experience.application.ReservationService;
import com.barofarm.support.experience.application.dto.ReservationServiceResponse;
import com.barofarm.support.experience.domain.ReservationStatus;
import com.barofarm.support.experience.presentation.dto.ReservationRequest;
import com.barofarm.support.experience.presentation.dto.ReservationResponse;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

/** ReservationController 유닛 테스트 */
@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    private UUID experienceId;
    private UUID buyerId;
    private UUID reservationId;
    private UUID userId;
    private ReservationRequest request;
    private ReservationServiceResponse serviceResponse;

    @BeforeEach
    void setUp() {
        experienceId = UUID.randomUUID();
        buyerId = UUID.randomUUID();
        reservationId = UUID.randomUUID();
        userId = buyerId; // 기본적으로 buyerId와 동일하게 설정

        request = new ReservationRequest(experienceId, buyerId, LocalDate.of(2025, 3, 15), "10:00-12:00", 2,
                BigInteger.valueOf(30000));

        serviceResponse = new ReservationServiceResponse(reservationId, experienceId, buyerId,
                LocalDate.of(2025, 3, 15), "10:00-12:00", 2, BigInteger.valueOf(30000), ReservationStatus.REQUESTED,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /api/reservations - 예약 생성")
    void createReservation() {
        when(reservationService.createReservation(eq(userId), any())).thenReturn(serviceResponse);

        ResponseDto<ReservationResponse> result = reservationController.createReservation(userId, request);

        assertThat(result).isNotNull();
        assertThat(result.data()).isNotNull();
        assertThat(result.data().getHeadCount()).isEqualTo(2);
        assertThat(result.data().getStatus()).isEqualTo(ReservationStatus.REQUESTED);
        verify(reservationService, times(1)).createReservation(eq(userId), any());
    }

    @Test
    @DisplayName("GET /api/reservations/{reservationId} - ID로 예약 조회")
    void getReservationById() {
        // given
        when(reservationService.getReservationById(eq(userId), eq(reservationId))).thenReturn(serviceResponse);

        // when
        ResponseDto<ReservationResponse> result = reservationController.getReservationById(userId, reservationId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isNotNull();
        assertThat(result.data().getReservationId()).isEqualTo(reservationId);
        verify(reservationService, times(1)).getReservationById(eq(userId), eq(reservationId));
    }

    @Test
    @DisplayName("GET /api/reservations?experienceId=xxx - 체험 ID로 예약 목록 조회")
    void getReservationsByExperienceId() {
        // given
        UUID sellerId = UUID.randomUUID();
        UUID reservationId2 = UUID.randomUUID();
        ReservationServiceResponse serviceResponse2 = new ReservationServiceResponse(reservationId2, experienceId,
                buyerId, LocalDate.of(2025, 3, 16), "14:00-16:00", 3, BigInteger.valueOf(45000),
                ReservationStatus.CONFIRMED, LocalDateTime.now(), LocalDateTime.now());

        Pageable pageable = PageRequest.of(0, 10);
        Page<ReservationServiceResponse> servicePage = new PageImpl<>(
                java.util.Arrays.asList(serviceResponse, serviceResponse2), pageable, 2);
        when(reservationService.getReservationsByExperienceId(eq(sellerId), eq(experienceId), any(Pageable.class)))
                .thenReturn(servicePage);

        // when
        ResponseDto<CustomPage<ReservationResponse>> result = reservationController.getReservations(sellerId, experienceId, null,
                pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isNotNull();
        assertThat(result.data().content()).hasSize(2);
        assertThat(result.data().content()).extracting("experienceId").containsOnly(experienceId);
        verify(reservationService, times(1)).getReservationsByExperienceId(eq(sellerId), eq(experienceId), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/reservations?buyerId=xxx - 구매자 ID로 예약 목록 조회")
    void getReservationsByBuyerId() {
        // given
        UUID otherExperienceId = UUID.randomUUID();
        ReservationServiceResponse serviceResponse2 = new ReservationServiceResponse(UUID.randomUUID(),
                otherExperienceId, buyerId, LocalDate.of(2025, 3, 16), "14:00-16:00", 1, BigInteger.valueOf(15000),
                ReservationStatus.REQUESTED, LocalDateTime.now(), LocalDateTime.now());

        Pageable pageable = PageRequest.of(0, 10);
        Page<ReservationServiceResponse> servicePage = new PageImpl<>(
                java.util.Arrays.asList(serviceResponse, serviceResponse2), pageable, 2);
        when(reservationService.getReservationsByBuyerId(eq(userId), eq(buyerId), any(Pageable.class))).thenReturn(servicePage);

        // when
        ResponseDto<CustomPage<ReservationResponse>> result = reservationController.getReservations(userId, null, buyerId,
                pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isNotNull();
        assertThat(result.data().content()).hasSize(2);
        assertThat(result.data().content()).extracting("buyerId").containsOnly(buyerId);
        verify(reservationService, times(1)).getReservationsByBuyerId(eq(userId), eq(buyerId), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/reservations - experienceId와 buyerId가 모두 없으면 빈 페이지를 반환한다")
    void getReservations_NoParams() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        ResponseDto<CustomPage<ReservationResponse>> result = reservationController.getReservations(userId, null, null,
                pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isNotNull();
        assertThat(result.data().content()).isEmpty();
        verify(reservationService, never()).getReservationsByExperienceId(any(), any(), any());
        verify(reservationService, never()).getReservationsByBuyerId(any(), any(), any());
    }

    @Test
    @DisplayName("PUT /api/reservations/{reservationId}/status - 예약 상태 변경")
    void updateReservationStatus() {
        // given
        UUID sellerId = UUID.randomUUID();
        ReservationServiceResponse updatedServiceResponse = new ReservationServiceResponse(reservationId, experienceId,
                buyerId, LocalDate.of(2025, 3, 15), "10:00-12:00", 2, BigInteger.valueOf(30000),
                ReservationStatus.CONFIRMED, LocalDateTime.now(), LocalDateTime.now());

        when(reservationService.updateReservationStatus(eq(sellerId), eq(reservationId), eq(ReservationStatus.CONFIRMED)))
                .thenReturn(updatedServiceResponse);

        // when
        ResponseDto<ReservationResponse> result = reservationController.updateReservationStatus(sellerId, reservationId,
                ReservationStatus.CONFIRMED);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isNotNull();
        assertThat(result.data().getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(reservationService, times(1)).updateReservationStatus(eq(sellerId), eq(reservationId), eq(ReservationStatus.CONFIRMED));
    }

    @Test
    @DisplayName("DELETE /api/reservations/{reservationId} - 예약 삭제")
    void deleteReservation() {
        // given
        doNothing().when(reservationService).deleteReservation(userId, reservationId);

        // when
        ResponseDto<Void> result = reservationController.deleteReservation(userId, reservationId);

        // then
        assertThat(result).isNotNull();
        verify(reservationService, times(1)).deleteReservation(userId, reservationId);
    }
}
