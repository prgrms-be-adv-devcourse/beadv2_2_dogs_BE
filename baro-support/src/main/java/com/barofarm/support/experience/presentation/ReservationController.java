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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 예약 REST API 컨트롤러 */
@RestController
@RequiredArgsConstructor
public class ReservationController implements ReservationSwaggerApi {

    private final ReservationService reservationService;

    @Override
    public ResponseDto<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        ReservationServiceResponse serviceResponse = reservationService.createReservation(request.toServiceRequest());
        return ResponseDto.ok(ReservationResponse.from(serviceResponse));
    }

    @Override
    public ResponseDto<ReservationResponse> getReservationById(@PathVariable("reservationId") UUID reservationId) {
        ReservationServiceResponse serviceResponse = reservationService.getReservationById(reservationId);
        return ResponseDto.ok(ReservationResponse.from(serviceResponse));
    }

    @Override
    public ResponseDto<CustomPage<ReservationResponse>> getReservations(
        @RequestParam(required = false) UUID experienceId,
        @RequestParam(required = false) UUID buyerId,
        Pageable pageable) {

        var servicePage = experienceId != null
            ? reservationService.getReservationsByExperienceId(experienceId, pageable)
            : buyerId != null
                ? reservationService.getReservationsByBuyerId(buyerId, pageable)
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
        @PathVariable("reservationId") UUID reservationId,
        @RequestParam ReservationStatus status) {
        ReservationServiceResponse serviceResponse = reservationService.updateReservationStatus(reservationId, status);
        return ResponseDto.ok(ReservationResponse.from(serviceResponse));
    }

    @Override
    public ResponseDto<Void> deleteReservation(@PathVariable("reservationId") UUID reservationId) {
        reservationService.deleteReservation(reservationId);
        return ResponseDto.ok(null);
    }
}
