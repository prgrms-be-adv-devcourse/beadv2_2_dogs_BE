package com.barofarm.seller.farm.presentation;

import com.barofarm.seller.common.response.CustomPage;
import com.barofarm.seller.common.response.ResponseDto;
import com.barofarm.seller.farm.application.FarmService;
import com.barofarm.seller.farm.application.dto.response.FarmCreateInfo;
import com.barofarm.seller.farm.application.dto.response.FarmDetailInfo;
import com.barofarm.seller.farm.application.dto.response.FarmListInfo;
import com.barofarm.seller.farm.application.dto.response.FarmUpdateInfo;
import com.barofarm.seller.farm.presentation.dto.FarmCreateRequestDto;
import com.barofarm.seller.farm.presentation.dto.FarmUpdateRequestDto;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${api.v1}/farms")
@RequiredArgsConstructor
public class FarmController implements FarmSwaggerApi {

    private final FarmService farmService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto<FarmCreateInfo> createFarm(
            @RequestPart("data") @Valid FarmCreateRequestDto request,
            @RequestPart("image") MultipartFile image) {
        UUID mockSellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        return farmService.createFarm(mockSellerId, request.toCommand(), image);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto<FarmUpdateInfo> updateFarm(
            @PathVariable("id") UUID id,
            @RequestPart("data") @Valid FarmUpdateRequestDto request,
            @RequestPart("image") MultipartFile image) {
        UUID mockSellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        return farmService.updateFarm(mockSellerId, id, request.toCommand(), image);
    }

    @GetMapping("/{id}")
    public ResponseDto<FarmDetailInfo> findFarm(@PathVariable("id") UUID id) {
        return farmService.findFarm(id);
    }

    @GetMapping
    public ResponseDto<CustomPage<FarmListInfo>> findFarmList(Pageable pageable) {
        return farmService.findFarmList(pageable);
    }

    @DeleteMapping("/{id}")
    public ResponseDto<Void> deleteFarm(@PathVariable("id") UUID id) {
        UUID mockSellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        farmService.deleteFarm(mockSellerId, id);
        return ResponseDto.ok(null);
    }
}
