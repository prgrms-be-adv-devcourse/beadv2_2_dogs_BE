package com.barofarm.support.experience.presentation;

import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.experience.domain.ReservationStatus;
import com.barofarm.support.experience.presentation.dto.ReservationRequest;
import com.barofarm.support.experience.presentation.dto.ReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reservation", description = "체험 예약 관리 API")
@RequestMapping("${api.v1}/reservations")
public interface ReservationSwaggerApi {

    @Operation(summary = "예약 등록", description = "새로운 체험 예약을 등록합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "예약 생성 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "요청 값 검증 실패",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping
    ResponseDto<ReservationResponse> createReservation(
        @Parameter(description = "사용자 ID (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Id") UUID userId,
        @Parameter(description = "사용자 이메일 (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Email") UUID userEmail,
        @Parameter(description = "사용자 역할 (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Role") String userRole,
        @Valid @RequestBody ReservationRequest request
    );

    @Operation(summary = "예약 상세 조회", description = "예약 ID로 예약 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "예약을 찾을 수 없음 (RESERVATION_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/{reservationId}")
    ResponseDto<ReservationResponse> getReservationById(
        @Parameter(description = "사용자 ID (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Id") UUID userId,
        @Parameter(description = "사용자 이메일 (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Email") UUID userEmail,
        @Parameter(description = "사용자 역할 (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Role") String userRole,
        @Parameter(description = "예약 ID", required = true) @PathVariable("reservationId") UUID reservationId
    );

    @Operation(summary = "예약 목록 조회", description = "예약 목록을 조회합니다. experienceId 또는 buyerId를 지정할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    ResponseDto<CustomPage<ReservationResponse>> getReservations(
        @Parameter(description = "사용자 ID (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Id") UUID userId,
        @Parameter(description = "사용자 이메일 (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Email") UUID userEmail,
        @Parameter(description = "사용자 역할 (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Role") String userRole,
        @Parameter(description = "체험 ID (선택사항)", required = false) @RequestParam(required = false) UUID experienceId,
        @Parameter(description = "구매자 ID (선택사항)", required = false) @RequestParam(required = false) UUID buyerId,
        Pageable pageable
    );

    @Operation(summary = "예약 상태 변경", description = "예약 상태를 변경합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "상태 변경 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "예약을 찾을 수 없음 (RESERVATION_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        )
    })
    @PutMapping("/{reservationId}/status")
    ResponseDto<ReservationResponse> updateReservationStatus(
        @Parameter(description = "사용자 ID (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Id") UUID userId,
        @Parameter(description = "사용자 이메일 (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Email") UUID userEmail,
        @Parameter(description = "사용자 역할 (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Role") String userRole,
        @Parameter(description = "예약 ID", required = true) @PathVariable("reservationId") UUID reservationId,
        @Parameter(description = "변경할 상태", required = true) @RequestParam ReservationStatus status
    );

    @Operation(summary = "예약 삭제", description = "예약을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "삭제 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "예약을 찾을 수 없음 (RESERVATION_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        )
    })
    @DeleteMapping("/{reservationId}")
    ResponseDto<Void> deleteReservation(
        @Parameter(description = "사용자 ID (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Id") UUID userId,
        @Parameter(description = "사용자 이메일 (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Email") UUID userEmail,
        @Parameter(description = "사용자 역할 (헤더에서 자동 전달)", hidden = true) @RequestHeader("X-User-Role") String userRole,
        @Parameter(description = "예약 ID", required = true) @PathVariable("reservationId") UUID reservationId
    );
}
