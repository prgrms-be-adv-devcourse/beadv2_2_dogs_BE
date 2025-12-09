package com.barofarm.support.experience.presentation;

import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.experience.presentation.dto.ExperienceRequest;
import com.barofarm.support.experience.presentation.dto.ExperienceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Experience", description = "체험 프로그램 관리 API")
@RequestMapping("${api.v1}/experiences")
public interface ExperienceSwaggerApi {

    @Operation(summary = "체험 프로그램 등록", description = "새로운 체험 프로그램을 등록합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "체험 프로그램 생성 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping
    ResponseDto<ExperienceResponse> createExperience(@Valid @RequestBody ExperienceRequest request);

    @Operation(summary = "체험 프로그램 상세 조회", description = "체험 ID로 체험 프로그램 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "체험 프로그램을 찾을 수 없음 (EXPERIENCE_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/{id}")
    ResponseDto<ExperienceResponse> getExperienceById(
        @Parameter(description = "체험 프로그램 ID", required = true) @PathVariable("id") UUID id
    );

    @Operation(summary = "체험 프로그램 목록 조회", description = "체험 프로그램 목록을 조회합니다. farmId를 지정하면 해당 농장의 체험만 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    ResponseDto<CustomPage<ExperienceResponse>> getExperiences(
        @Parameter(description = "농장 ID (선택사항)", required = false) @RequestParam(required = false) UUID farmId,
        Pageable pageable
    );

    @Operation(summary = "체험 프로그램 수정", description = "기존 체험 프로그램 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "수정 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "체험 프로그램을 찾을 수 없음 (EXPERIENCE_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        )
    })
    @PutMapping("/{id}")
    ResponseDto<ExperienceResponse> updateExperience(
        @Parameter(description = "체험 프로그램 ID", required = true) @PathVariable("id") UUID id,
        @Valid @RequestBody ExperienceRequest request
    );

    @Operation(summary = "체험 프로그램 삭제", description = "체험 프로그램을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "삭제 성공",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "체험 프로그램을 찾을 수 없음 (EXPERIENCE_NOT_FOUND)",
            content = @Content(mediaType = "application/json")
        )
    })
    @DeleteMapping("/{id}")
    ResponseDto<Void> deleteExperience(
        @Parameter(description = "체험 프로그램 ID", required = true) @PathVariable("id") UUID id
    );
}
