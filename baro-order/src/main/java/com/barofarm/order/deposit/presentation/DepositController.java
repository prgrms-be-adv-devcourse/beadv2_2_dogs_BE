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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("${api.v1}/deposits")
@RequiredArgsConstructor
// TODO: 나중에 인증/인가 붙이면 @RequestHeader("userId") UUID sellerId 사용
public class DepositController {

    private final DepositService depositService;

    @PostMapping("/charges")
    public ResponseDto<DepositChargeCreateInfo> createCharge(@Valid @RequestBody DepositChargeCreateRequest request) {
        UUID mockUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        return depositService.createCharge(mockUserId, request.toCommand());
    }

    @GetMapping
    public ResponseDto<DepositInfo> findDeposit() {
        UUID mockUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        return depositService.findDeposit(mockUserId);
    }

    @PostMapping("/pay")
    public ResponseDto<DepositPaymentInfo> payDeposit(@RequestBody DepositPaymentRequest request) {
        UUID mockUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        return depositService.payDeposit(mockUserId, request.toCommand());
    }

    @PostMapping("/refund")
    public ResponseDto<DepositRefundInfo> refundDeposit(@Valid @RequestBody DepositRefundRequest request) {
        UUID mockUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        return depositService.refundDeposit(mockUserId, request.toCommand());
    }
}
