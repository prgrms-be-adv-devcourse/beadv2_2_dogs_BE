package com.barofarm.seller.farm.presentation;

import com.barofarm.seller.farm.application.dto.response.FarmCreateInfo;
import com.barofarm.seller.farm.application.dto.response.FarmDetailInfo;
import com.barofarm.seller.farm.application.dto.response.FarmUpdateInfo;
import com.barofarm.seller.farm.application.FarmService;
import com.barofarm.seller.farm.presentation.dto.FarmCreateRequestDto;
import com.barofarm.seller.farm.presentation.dto.FarmUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.v1}/farms")
@RequiredArgsConstructor
// TODO: 나중에 인증/인가 붙이면 @RequestHeader("userId") UUID sellerId 사용
public class FarmController {

    private final FarmService farmService;

    @Operation(summary = "농장 정보 등록", description = "농장 정보를 등록한다.")
    @PostMapping
    public ResponseEntity<FarmCreateInfo> createFarm(@Valid @RequestBody FarmCreateRequestDto request) {

        UUID mockSellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        return farmService.createFarm(mockSellerId, request.toCommand());
    }

    @Operation(summary = "농장 정보 수정", description = "농장 정보를 수정한다.")
    @PutMapping("/{id}")
    public ResponseEntity<FarmUpdateInfo> updateFarm(@PathVariable("id") UUID id,
                                                     @Valid @RequestBody FarmUpdateRequestDto request) {

        return farmService.updateFarm(id, request.toCommand());
    }

    @Operation(summary = "농장 정보 상세 조회", description = "농장 정보를 조회한다.")
    @GetMapping("/{id}")
    public ResponseEntity<FarmDetailInfo> findFarm(@PathVariable("id") UUID id) {

        return farmService.findFarm(id);
    }

    @Operation(summary = "농장 목록 조회", description = "농장 정보를 페이지 단위로 조회한다.")
    @GetMapping
    public ResponseEntity<List<FarmDetailInfo>> findFarmList(Pageable pageable) {

        return farmService.findFarmList(pageable);
    }

    @Operation(summary = "농장 삭제", description = "농장을 삭제한다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFarm(@PathVariable("id") UUID id) {
        farmService.deleteFarm(id);
        return ResponseEntity.noContent().build();
    }
}
