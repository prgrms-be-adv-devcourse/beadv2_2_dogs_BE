package com.barofarm.order.deposit.presentation;

import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.deposit.application.DepositService;
import com.barofarm.order.deposit.application.dto.response.DepositChargeCreateInfo;
import com.barofarm.order.deposit.application.dto.response.DepositInfo;
import com.barofarm.order.deposit.application.dto.response.DepositPaymentInfo;
import com.barofarm.order.deposit.application.dto.response.DepositRefundInfo;
import com.barofarm.order.deposit.presentation.dto.DepositChargeCreateRequest;
import com.barofarm.order.deposit.presentation.dto.DepositPaymentRequest;
import com.barofarm.order.deposit.presentation.dto.DepositRefundRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.v1}/deposits")
@RequiredArgsConstructor
// TODO: 나중에 인증/인가 붙이면 @RequestHeader("userId") UUID sellerId 사용
public class DepositController implements DepositSwaggerApi{

    private final DepositService depositService;

    @PostMapping("/charges")
    public ResponseDto<DepositChargeCreateInfo> createCharge(
        @RequestHeader("X-User-Id") UUID userId,
        @Valid @RequestBody DepositChargeCreateRequest request) {
        return depositService.createCharge(userId, request.toCommand());
    }

    @GetMapping
    public ResponseDto<DepositInfo> findDeposit(@RequestHeader("X-User-Id") UUID userId) {
        return depositService.findDeposit(userId);
    }

    @PostMapping("/pay")
    public ResponseDto<DepositPaymentInfo> payDeposit(
        @RequestHeader("X-User-Id") UUID userId,
        @RequestBody DepositPaymentRequest request) {
        return depositService.payDeposit(userId, request.toCommand());
    }

    @PostMapping("/refund")
    public ResponseDto<DepositRefundInfo> refundDeposit(
        @RequestHeader("X-User-Id") UUID userId,
        @Valid @RequestBody DepositRefundRequest request) {
        return depositService.refundDeposit(userId, request.toCommand());
    }
}
