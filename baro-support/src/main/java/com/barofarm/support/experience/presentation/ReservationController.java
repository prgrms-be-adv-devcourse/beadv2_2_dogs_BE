package com.barofarm.support.experience.presentation;

import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.experience.application.ReservationService;
import com.barofarm.support.experience.application.dto.ReservationServiceResponse;
import com.barofarm.support.experience.domain.ReservationStatus;
import com.barofarm.support.experience.presentation.dto.ReservationRequest;
import com.barofarm.support.experience.presentation.dto.ReservationResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 예약 REST API 컨트롤러 */
@RestController
@RequiredArgsConstructor
public class ReservationController implements ReservationSwaggerApi {

    private final ReservationService reservationService;

    @Override
    public ResponseDto<ReservationResponse> createReservation(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Email") UUID userEmail,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody ReservationRequest request
    ) {
        ReservationServiceResponse serviceResponse = reservationService.createReservation(userId, request.toServiceRequest());
        return ResponseDto.ok(ReservationResponse.from(serviceResponse));
    }

    @Override
    public ResponseDto<ReservationResponse> getReservationById(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Email") UUID userEmail,
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable("reservationId") UUID reservationId
    ) {
        ReservationServiceResponse serviceResponse = reservationService.getReservationById(userId, reservationId);
        return ResponseDto.ok(ReservationResponse.from(serviceResponse));
    }

    @Override
    public ResponseDto<CustomPage<ReservationResponse>> getReservations(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Email") UUID userEmail,
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam(required = false) UUID experienceId,
            @RequestParam(required = false) UUID buyerId,
            Pageable pageable
    ) {
        var servicePage = experienceId != null
            ? reservationService.getReservationsByExperienceId(userId, experienceId, pageable)
            : buyerId != null
                ? reservationService.getReservationsByBuyerId(userId, buyerId, pageable)
                : null;

        if (servicePage == null) {
            // 파라미터가 없으면 빈 페이지 반환
            return ResponseDto.ok(CustomPage.from(
                org.springframework.data.domain.Page.empty(pageable)
            ));
        }

        var responsePage = servicePage.map(ReservationResponse::from);
        return ResponseDto.ok(CustomPage.from(responsePage));
    }

    @Override
    public ResponseDto<ReservationResponse> updateReservationStatus(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Email") UUID userEmail,
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable("reservationId") UUID reservationId,
            @RequestParam ReservationStatus status
    ) {
        ReservationServiceResponse serviceResponse = reservationService.updateReservationStatus(userId, reservationId, status);
        return ResponseDto.ok(ReservationResponse.from(serviceResponse));
    }

    @Override
    public ResponseDto<Void> deleteReservation(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Email") UUID userEmail,
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable("reservationId") UUID reservationId
    ) {
        reservationService.deleteReservation(userId, reservationId);
        return ResponseDto.ok(null);
    }
}
