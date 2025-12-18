package com.barofarm.support.settlement.presentation;

import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.settlement.application.SettlementService;
import com.barofarm.support.settlement.application.dto.SettlementResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.v1}/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/me")
    public ResponseDto<SettlementResponse> getSettlement(
        @RequestHeader("X-User-Id") UUID sellerId
    ) {
        return ResponseDto.ok(settlementService.getSettlement(sellerId));
    }
}
