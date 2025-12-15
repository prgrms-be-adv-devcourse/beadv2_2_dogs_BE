package com.barofarm.seller.farm.presentation;

import com.barofarm.seller.common.response.CustomPage;
import com.barofarm.seller.common.response.ResponseDto;
import com.barofarm.seller.farm.application.dto.response.FarmCreateInfo;
import com.barofarm.seller.farm.application.dto.response.FarmDetailInfo;
import com.barofarm.seller.farm.application.dto.response.FarmListInfo;
import com.barofarm.seller.farm.application.dto.response.FarmUpdateInfo;
import com.barofarm.seller.farm.presentation.dto.FarmCreateRequestDto;
import com.barofarm.seller.farm.presentation.dto.FarmUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Farm", description = "농장 관련 API")
@RequestMapping("${api.v1}/farms")
public interface FarmSwaggerApi {

    @Operation(summary = "농장 정보 등록", description = "농장 정보를 등록한다. (multipart/form-data: data(JSON) + image(File))")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "농장 등록 성공", content = @Content(mediaType = "application/json")),

        // validation + image validate
        @ApiResponse(responseCode = "400", description =
            "요청 값 검증 실패 또는 이미지 검증 실패\n" +
                "- FARM_IMAGE_REQUIRED: 이미지 파트 누락/NULL\n" +
                "- FARM_IMAGE_EMPTY_FILE: 비어있는 이미지 파일\n" +
                "- FARM_IMAGE_INVALID_TYPE: 이미지 파일 타입 아님\n" +
                "- FARM_IMAGE_TOO_LARGE: 이미지 10MB 초과",
            content = @Content(mediaType = "application/json")
        ),

        // seller
        @ApiResponse(responseCode = "404", description = "판매자를 찾을 수 없음 (SELLER_NOT_FOUND)", content = @Content(mediaType = "application/json")),

        // s3
        @ApiResponse(responseCode = "500", description = "S3 업로드 실패 (FARM_IMAGE_UPLOAD_FAIL)", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseDto<FarmCreateInfo> createFarm(
        @Parameter(description = "농장 생성 정보(JSON). RequestPart name은 data", required = true)
        @RequestPart("data") @Valid FarmCreateRequestDto request,

        @Parameter(description = "농장 이미지 파일. RequestPart name은 image", required = true)
        @RequestPart("image") MultipartFile image
    );

    @Operation(summary = "농장 정보 수정", description = "농장 정보를 수정한다. (multipart/form-data: data(JSON) + image(File))")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "농장 수정 성공", content = @Content(mediaType = "application/json")),

        // validation + image validate
        @ApiResponse(responseCode = "400", description =
            "요청 값 검증 실패 또는 이미지 검증 실패\n" +
                "- FARM_IMAGE_REQUIRED: 이미지 파트 누락/NULL\n" +
                "- FARM_IMAGE_EMPTY_FILE: 비어있는 이미지 파일\n" +
                "- FARM_IMAGE_INVALID_TYPE: 이미지 파일 타입 아님\n" +
                "- FARM_IMAGE_TOO_LARGE: 이미지 10MB 초과",
            content = @Content(mediaType = "application/json")
        ),

        // auth
        @ApiResponse(responseCode = "403", description = "농장 수정 권한 없음 (FARM_FORBIDDEN)", content = @Content(mediaType = "application/json")),

        // not found
        @ApiResponse(responseCode = "404", description = "농장을 찾을 수 없음 (FARM_NOT_FOUND)", content = @Content(mediaType = "application/json")),

        // s3
        @ApiResponse(responseCode = "500", description =
            "S3 처리 실패\n" +
                "- FARM_IMAGE_UPLOAD_FAIL: 새 이미지 업로드 실패\n" +
                "- FARM_IMAGE_DELETE_FAIL: 기존 이미지 삭제 실패",
            content = @Content(mediaType = "application/json")
        )
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseDto<FarmUpdateInfo> updateFarm(
        @PathVariable("id") UUID id,

        @Parameter(description = "농장 수정 정보(JSON). RequestPart name은 data", required = true)
        @RequestPart("data") @Valid FarmUpdateRequestDto request,

        @Parameter(description = "새 농장 이미지 파일. RequestPart name은 image (필수)", required = true)
        @RequestPart("image") MultipartFile image
    );

    @Operation(summary = "농장 정보 상세 조회", description = "농장 정보를 조회한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "농장 조회 성공", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "농장을 찾을 수 없음 (FARM_NOT_FOUND)", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{id}")
    ResponseDto<FarmDetailInfo> findFarm(@PathVariable("id") UUID id);

    @Operation(summary = "농장 목록 조회", description = "농장 정보를 페이지 단위로 조회한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "농장 목록 조회 성공", content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    ResponseDto<CustomPage<FarmListInfo>> findFarmList(Pageable pageable);

    @Operation(summary = "농장 삭제", description = "농장을 삭제한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "농장 삭제 성공", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "농장 삭제 권한 없음 (FARM_FORBIDDEN)", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "농장을 찾을 수 없음 (FARM_NOT_FOUND)", content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{id}")
    ResponseDto<Void> deleteFarm(@PathVariable("id") UUID id);
}
