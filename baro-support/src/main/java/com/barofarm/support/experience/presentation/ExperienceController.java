package com.barofarm.support.experience.presentation;

import com.barofarm.support.experience.application.ExperienceService;
import com.barofarm.support.experience.application.dto.ExperienceServiceResponse;
import com.barofarm.support.experience.presentation.dto.ExperienceRequest;
import com.barofarm.support.experience.presentation.dto.ExperienceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 체험 프로그램 REST API 컨트롤러 */
@Tag(name = "Experience", description = "체험 프로그램 관리 API")
@RestController
@RequestMapping("/api/experiences")
@RequiredArgsConstructor
public class ExperienceController {

    private final ExperienceService experienceService;

    /**
     * 체험 프로그램 생성
     *
     * @param request
     *            체험 프로그램 생성 요청
     * @return 생성된 체험 프로그램
     */
    @Operation(summary = "체험 프로그램 등록", description = "새로운 체험 프로그램을 등록합니다.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "체험 프로그램 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")})
    @PostMapping
    public ResponseEntity<ExperienceResponse> createExperience(@Valid @RequestBody ExperienceRequest request) {
        // Command DTO → Service DTO 변환
        ExperienceServiceResponse serviceResponse = experienceService.createExperience(request.toServiceRequest());
        // Service DTO → Presentation DTO 변환
        return ResponseEntity.status(HttpStatus.CREATED).body(ExperienceResponse.from(serviceResponse));
    }

    /**
     * ID로 체험 프로그램 조회
     *
     * @param id
     *            체험 ID
     * @return 체험 프로그램
     */
    @Operation(summary = "체험 프로그램 상세 조회", description = "체험 ID로 체험 프로그램 상세 정보를 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "체험 프로그램을 찾을 수 없음")})
    @GetMapping("/{id}")
    public ResponseEntity<ExperienceResponse> getExperienceById(
            @Parameter(description = "체험 프로그램 ID", required = true) @PathVariable Long id) {
        ExperienceServiceResponse serviceResponse = experienceService.getExperienceById(id);
        return ResponseEntity.ok(ExperienceResponse.from(serviceResponse));
    }

    /**
     * 농장 ID로 체험 프로그램 목록 조회
     *
     * @param farmId
     *            농장 ID
     * @return 체험 프로그램 목록
     */
    @Operation(summary = "체험 프로그램 목록 조회", description = "체험 프로그램 목록을 조회합니다. farmId를 지정하면 해당 농장의 체험만 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공")})
    @GetMapping
    public ResponseEntity<List<ExperienceResponse>> getExperiences(
            @Parameter(description = "농장 ID (선택사항)", required = false) @RequestParam(required = false) Long farmId) {
        List<ExperienceServiceResponse> serviceResponses;

        if (farmId != null) {
            serviceResponses = experienceService.getExperiencesByFarmId(farmId);
        } else {
            serviceResponses = experienceService.getAllExperiences();
        }

        List<ExperienceResponse> responses = serviceResponses.stream().map(ExperienceResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 체험 프로그램 수정
     *
     * @param id
     *            체험 ID
     * @param request
     *            체험 프로그램 수정 요청
     * @return 수정된 체험 프로그램
     */
    @Operation(summary = "체험 프로그램 수정", description = "기존 체험 프로그램 정보를 수정합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "체험 프로그램을 찾을 수 없음")})
    @PutMapping("/{id}")
    public ResponseEntity<ExperienceResponse> updateExperience(
            @Parameter(description = "체험 프로그램 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ExperienceRequest request) {
        ExperienceServiceResponse serviceResponse = experienceService.updateExperience(id, request.toServiceRequest());
        return ResponseEntity.ok(ExperienceResponse.from(serviceResponse));
    }

    /**
     * 체험 프로그램 삭제
     *
     * @param id
     *            체험 ID
     * @return 삭제 완료 응답
     */
    @Operation(summary = "체험 프로그램 수정", description = "체험 프로그램을 삭제합니다.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "체험 프로그램을 찾을 수 없음")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExperience(
            @Parameter(description = "체험 프로그램 ID", required = true) @PathVariable Long id) {
        experienceService.deleteExperience(id);
        return ResponseEntity.noContent().build();
    }
}
