package com.barofarm.seller.farm.presentation;

import com.barofarm.seller.common.response.CustomPage;
import com.barofarm.seller.common.response.ResponseDto;
import com.barofarm.seller.farm.application.FarmService;
import com.barofarm.seller.farm.application.dto.response.FarmCreateInfo;
import com.barofarm.seller.farm.application.dto.response.FarmDetailInfo;
import com.barofarm.seller.farm.application.dto.response.FarmUpdateInfo;
import com.barofarm.seller.farm.presentation.dto.FarmCreateRequestDto;
import com.barofarm.seller.farm.presentation.dto.FarmUpdateRequestDto;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.v1}/farms")
@RequiredArgsConstructor
// TODO: 나중에 인증/인가 붙이면 @RequestHeader("userId") UUID sellerId 사용
public class FarmController implements FarmSwaggerApi{

    private final FarmService farmService;

    @PostMapping
    public ResponseDto<FarmCreateInfo> createFarm(@Valid @RequestBody FarmCreateRequestDto request) {
        UUID mockSellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        return farmService.createFarm(mockSellerId, request.toCommand());
    }

    @PutMapping("/{id}")
    public ResponseDto<FarmUpdateInfo> updateFarm(@PathVariable("id") UUID id,
                                                     @Valid @RequestBody FarmUpdateRequestDto request) {
        return farmService.updateFarm(id, request.toCommand());
    }

    @GetMapping("/{id}")
    public ResponseDto<FarmDetailInfo> findFarm(@PathVariable("id") UUID id) {
        return farmService.findFarm(id);
    }

    @GetMapping
    public ResponseDto<CustomPage<FarmDetailInfo>> findFarmList(Pageable pageable) {
        return farmService.findFarmList(pageable);
    }

    @DeleteMapping("/{id}")
    public ResponseDto<Void> deleteFarm(@PathVariable("id") UUID id) {
        farmService.deleteFarm(id);
        return ResponseDto.ok(null);
    }
}
