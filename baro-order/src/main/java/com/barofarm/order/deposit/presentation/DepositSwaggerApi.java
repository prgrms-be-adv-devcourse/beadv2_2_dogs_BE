package com.barofarm.order.deposit.presentation;

import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.deposit.application.dto.response.DepositChargeCreateInfo;
import com.barofarm.order.deposit.application.dto.response.DepositCreateInfo;
import com.barofarm.order.deposit.application.dto.response.DepositInfo;
import com.barofarm.order.deposit.application.dto.response.DepositPaymentInfo;
import com.barofarm.order.deposit.application.dto.response.DepositRefundInfo;
import com.barofarm.order.deposit.presentation.dto.DepositChargeCreateRequest;
import com.barofarm.order.deposit.presentation.dto.DepositPaymentRequest;
import com.barofarm.order.deposit.presentation.dto.DepositRefundRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Deposit", description = "예치금 관련 API")
@RequestMapping("${api.v1}/deposits")
public interface DepositSwaggerApi {

    @Operation(
            summary = "예치금 충전 요청 생성",
            description =
                    "예치금 충전을 위한 충전 요청(DepositCharge)을 생성한다. "
                            + "실제 충전은 Toss 결제 승인 이후 반영된다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "충전 요청 생성 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "예치금 계정이 존재하지 않음 (DEPOSIT_NOT_FOUND)",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/charges")
    ResponseDto<DepositChargeCreateInfo> createCharge(
            @Parameter(
                    description = "요청 사용자 ID (X-User-Id 헤더)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader("X-User-Id") UUID userId,

            @Parameter(description = "충전 요청 정보", required = true)
            @Valid @RequestBody DepositChargeCreateRequest request
    );

    @Operation(
            summary = "예치금 조회",
            description = "사용자의 예치금 잔액을 조회한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "예치금 조회 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "예치금 계정이 존재하지 않음 (DEPOSIT_NOT_FOUND)",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping
    ResponseDto<DepositInfo> findDeposit(
            @Parameter(
                    description = "요청 사용자 ID (X-User-Id 헤더)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader("X-User-Id") UUID userId
    );

    @Operation(
            summary = "예치금으로 주문 결제",
            description = "예치금을 차감하여 주문을 결제 완료 처리한다. 결제(Payment) 내역을 생성한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "예치금 결제 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "예치금 부족 (INSUFFICIENT_DEPOSIT_BALANCE)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "예치금 계정이 존재하지 않음 (DEPOSIT_NOT_FOUND)",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/pay")
    ResponseDto<DepositPaymentInfo> payDeposit(
            @Parameter(
                    description = "요청 사용자 ID (X-User-Id 헤더)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader("X-User-Id") UUID userId,

            @Parameter(description = "결제 요청 정보", required = true)
            @RequestBody DepositPaymentRequest request
    );

    @Operation(
            summary = "예치금 결제 환불",
            description =
                    "예치금 결제로 처리된 주문을 환불한다. 결제(Payment)를 REFUNDED로 변경하고 예치금을 복구한다. "
                            + "이미 환불된 경우 멱등 처리로 0원 환불로 응답할 수 있다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "예치금 환불 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description =
                            "예치금 계정/결제 내역이 존재하지 않음 "
                                    + "(DEPOSIT_NOT_FOUND, DEPOSIT_PAYMENT_NOT_FOUND)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "환불 불가 상태 (정책에 따라 status 체크 시)",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/refund")
    ResponseDto<DepositRefundInfo> refundDeposit(
            @Parameter(
                    description = "요청 사용자 ID (X-User-Id 헤더)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader("X-User-Id") UUID userId,

            @Parameter(description = "환불 요청 정보", required = true)
            @Valid @RequestBody DepositRefundRequest request
    );

    @Operation(
            summary = "예치금 계정 생성",
            description = "사용자 예치금 계정을 생성한다. (없을 때만 생성되는 용도)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "예치금 계정 생성 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 예치금 계정이 존재함 (DEPOSIT_ALREADY_EXISTS 등)",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/create")
    ResponseDto<DepositCreateInfo> createDeposit(
            @Parameter(
                    description = "요청 사용자 ID (X-User-Id 헤더)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader("X-User-Id") UUID userId
    );
}
