package com.barofarm.seller.seller.presentation;

import com.barofarm.seller.common.response.ResponseDto;
import com.barofarm.seller.seller.application.SellerService;
import com.barofarm.seller.seller.presentation.dto.SellerApplyRequestDto;
import com.barofarm.seller.seller.presentation.dto.SellerInfoDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // 단건 조회
    @GetMapping("/user/{userId}")
    public ResponseDto<SellerInfoDto> getByUserId(@PathVariable("userId") UUID userId) {

        SellerInfoDto info = sellerService.getASellerByUserId(userId);
        return ResponseDto.ok(info);
    }

    // 벌크로 조회
    @GetMapping("/users/bulks")
    public ResponseDto<List<SellerInfoDto>> getByUsers(@RequestBody List<UUID> userIds) {

        List<SellerInfoDto> infos = sellerService.getSellersByIds(userIds);
        return ResponseDto.ok(infos);
    }

}
