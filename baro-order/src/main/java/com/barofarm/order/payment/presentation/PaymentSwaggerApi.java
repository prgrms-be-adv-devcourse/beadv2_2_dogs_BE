package com.barofarm.order.payment.presentation;

import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.payment.application.dto.response.TossPaymentConfirmInfo;
import com.barofarm.order.payment.application.dto.response.TossPaymentRefundInfo;
import com.barofarm.order.payment.presentation.dto.TossPaymentConfirmRequest;
import com.barofarm.order.payment.presentation.dto.TossPaymentRefundRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Payment", description = "결제 관련 API")
@RequestMapping("${api.v1}/payments/toss")
public interface PaymentSwaggerApi {

    @Operation(
        summary = "토스 결제 승인",
        description = "Toss Payments 결제 승인 API를 호출하여 결제를 확정하고, 주문을 결제 완료 상태로 변경한다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "결제 승인 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "요청 값 검증 실패",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "유효한 Toss Secret Key가 설정되어 있지 않음 (INVALID_SECRET_KEY)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음 (ORDER_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/confirm")
    ResponseDto<TossPaymentConfirmInfo> confirmPayment(
        @Valid @RequestBody TossPaymentConfirmRequest confirmRequest
    );

    @Operation(
            summary = "토스 결제 환불",
            description = "Toss Payments 결제 취소(환불) API를 호출하여 결제를 환불 처리하고, 주문을 취소 상태로 변경한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "환불 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 환불 요청 (TOSS_PAYMENT_INVALID_REQUEST)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Toss 인증 실패 또는 Secret Key 오류 (INVALID_SECRET_KEY, TOSS_PAYMENT_UNAUTHORIZED)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "주문을 찾을 수 없음 (ORDER_NOT_FOUND)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 취소된 결제이거나 취소 불가 상태 (TOSS_PAYMENT_CONFLICT)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Toss 서버 오류 또는 결제 취소 실패 (TOSS_PAYMENT_SERVER_ERROR, TOSS_PAYMENT_CANCEL_FAILED)",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/refund")
    ResponseDto<TossPaymentRefundInfo> refundPayment(
            @Valid @RequestBody TossPaymentRefundRequest refundRequest
    );

    @Operation(
        summary = "토스 예치금 충전 승인",
        description = "Toss Payments 결제 승인 API를 호출하여 예치금 충전 결제를 확정하고, 예치금 충전(DepositCharge)을 성공 처리한 뒤 결제(Payment) 내역을 생성한다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "예치금 충전 승인 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "요청 값 검증 실패 또는 잘못된 승인 요청 (TOSS_PAYMENT_INVALID_REQUEST)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Toss 인증 실패 또는 Secret Key 오류 (INVALID_SECRET_KEY, TOSS_PAYMENT_UNAUTHORIZED)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "예치금 충전 요청을 찾을 수 없음 (DEPOSIT_CHARGE_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 처리된 충전 요청 또는 처리 불가 상태 (DEPOSIT_CHARGE_INVALID_STATUS, TOSS_PAYMENT_CONFLICT)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Toss 서버 오류 또는 결제 승인 실패 (TOSS_PAYMENT_SERVER_ERROR, TOSS_PAYMENT_CONFIRM_FAILED)",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/confirm/deposit")
    ResponseDto<TossPaymentConfirmInfo> confirmDeposit(
        @Valid @RequestBody TossPaymentConfirmRequest request
    );
}
