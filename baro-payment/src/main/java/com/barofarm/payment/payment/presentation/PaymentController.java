package com.barofarm.payment.payment.presentation;

import com.barofarm.dto.ResponseDto;
import com.barofarm.payment.payment.application.PaymentService;
import com.barofarm.payment.payment.application.dto.response.TossPaymentConfirmInfo;
import com.barofarm.payment.payment.presentation.dto.TossPaymentConfirmRequest;
import jakarta.validation.Valid;
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
public class PaymentController implements PaymentSwaggerApi {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseDto<TossPaymentConfirmInfo> confirmPayment(
        @RequestHeader("X-User-Id") UUID userId,
        @Valid @RequestBody TossPaymentConfirmRequest confirmRequest) {
        return paymentService.confirmPayment(userId, confirmRequest.toCommand());
    }

    @PostMapping("/confirm/deposit")
    public ResponseDto<TossPaymentConfirmInfo> confirmDeposit(
        @RequestHeader("X-User-Id") UUID userId,
        @Valid @RequestBody TossPaymentConfirmRequest request) {
        return paymentService.confirmDeposit(userId, request.toCommand());
    }
}
