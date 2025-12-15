package com.barofarm.order.deposit.presentation;

import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.deposit.application.DepositService;
import com.barofarm.order.deposit.application.dto.response.DepositCreateInfo;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class DepositInternalController {

    private final DepositService depositService;

    @PostMapping("internal/deposits/create")
    public ResponseDto<DepositCreateInfo> createDeposit(@RequestHeader("X-User-Id") UUID userId) {
        return depositService.createDeposit(userId);
    }
}
