package com.barofarm.order.payment.presentation;

import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.payment.application.PaymentService;
import com.barofarm.order.payment.application.dto.response.TossPaymentConfirmInfo;
import com.barofarm.order.payment.application.dto.response.TossPaymentRefundInfo;
import com.barofarm.order.payment.presentation.dto.TossPaymentConfirmRequest;
import com.barofarm.order.payment.presentation.dto.TossPaymentRefundRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.v1}/payments/toss")
@RequiredArgsConstructor
// TODO: 나중에 인증/인가 붙이면 @RequestHeader("userId") UUID sellerId 사용
public class PaymentController implements PaymentSwaggerApi{

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseDto<TossPaymentConfirmInfo> confirmPayment(
        @RequestHeader("X-User-Id") UUID userId,
        @RequestBody TossPaymentConfirmRequest confirmRequest) {
        return paymentService.confirmPayment(userId, confirmRequest.toCommand());
    }

    @PostMapping("/refund")
    public ResponseDto<TossPaymentRefundInfo> refundPayment(
        @RequestHeader("X-User-Id") UUID userId,
        @RequestBody TossPaymentRefundRequest refundRequest) {
        return paymentService.refundPayment(userId, refundRequest.toCommand());
    }

    @PostMapping("/confirm/deposit")
    public ResponseDto<TossPaymentConfirmInfo> confirmDeposit(
        @RequestHeader("X-User-Id") UUID userId,
        @RequestBody TossPaymentConfirmRequest request) {
        return paymentService.confirmDeposit(userId, request.toCommand());
    }
}
