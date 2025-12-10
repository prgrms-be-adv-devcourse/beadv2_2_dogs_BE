package com.barofarm.seller.farm.presentation;

import com.barofarm.seller.common.response.CustomPage;
import com.barofarm.seller.common.response.ResponseDto;
import com.barofarm.seller.farm.application.dto.response.FarmCreateInfo;
import com.barofarm.seller.farm.application.dto.response.FarmDetailInfo;
import com.barofarm.seller.farm.application.dto.response.FarmUpdateInfo;
import com.barofarm.seller.farm.presentation.dto.FarmCreateRequestDto;
import com.barofarm.seller.farm.presentation.dto.FarmUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Farm", description = "농장 관련 API")
@RequestMapping("${api.v1}/farms")
public interface FarmSwaggerApi {

    @Operation(summary = "농장 정보 등록", description = "농장 정보를 등록한다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "농장 등록 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "요청 값 검증 실패",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "판매자를 찾을 수 없음 (SELLER_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping
    ResponseDto<FarmCreateInfo> createFarm(@Valid @RequestBody FarmCreateRequestDto request);

    @Operation(summary = "농장 정보 수정", description = "농장 정보를 수정한다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "농장 수정 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "요청 값 검증 실패",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "농장 수정 권한 없음 (FARM_FORBIDDEN)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "농장을 찾을 수 없음 (FARM_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        )
    })
    @PutMapping("/{id}")
    ResponseDto<FarmUpdateInfo> updateFarm(
        @PathVariable("id") UUID id,
        @Valid @RequestBody FarmUpdateRequestDto request
    );

    @Operation(summary = "농장 정보 상세 조회", description = "농장 정보를 조회한다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "농장 조회 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "농장을 찾을 수 없음 (FARM_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/{id}")
    ResponseDto<FarmDetailInfo> findFarm(@PathVariable("id") UUID id);

    @Operation(summary = "농장 목록 조회", description = "농장 정보를 페이지 단위로 조회한다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "농장 목록 조회 성공",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    ResponseDto<CustomPage<FarmDetailInfo>> findFarmList(Pageable pageable);

    @Operation(summary = "농장 삭제", description = "농장을 삭제한다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "농장 삭제 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "농장 수정 권한 없음 (FARM_FORBIDDEN)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "농장을 찾을 수 없음 (FARM_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        )
    })
    @DeleteMapping("/{id}")
    ResponseDto<Void> deleteFarm(@PathVariable("id") UUID id);
}
