package com.barofarm.support.deposit.presentation;

import com.barofarm.dto.ResponseDto;
import com.barofarm.support.deposit.application.DepositService;
import com.barofarm.support.deposit.application.dto.response.DepositCreateInfo;
import com.barofarm.support.deposit.application.dto.response.DepositRefundInfo;
import com.barofarm.support.deposit.presentation.dto.DepositRefundRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/deposits")
@RequiredArgsConstructor
public class DepositInternalController {

    private final DepositService depositService;

    @PostMapping("/create")
    public ResponseDto<DepositCreateInfo> createDeposit(@RequestHeader("X-User-Id") UUID userId) {
        return depositService.createDeposit(userId);
    }

    @PostMapping("/refund")
    public ResponseDto<DepositRefundInfo> refundDeposit(
        @RequestHeader("X-User-Id") UUID userId,
        @Valid @RequestBody DepositRefundRequest request) {
        return depositService.refundDeposit(userId, request.toCommand());
    }
}
