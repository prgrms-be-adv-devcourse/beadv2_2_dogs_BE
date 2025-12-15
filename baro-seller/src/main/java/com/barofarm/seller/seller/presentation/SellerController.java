package com.barofarm.seller.seller.presentation;

import com.barofarm.seller.common.response.ResponseDto;
import com.barofarm.seller.seller.application.SellerService;
import com.barofarm.seller.seller.presentation.dto.SellerApplyRequestDto;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.v1}/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/apply")
    public ResponseDto<Void> applyForSeller(@RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody SellerApplyRequestDto request) {
        sellerService.applyForSeller(userId, request);
        return ResponseDto.ok(null);
    }
}
