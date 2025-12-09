package com.barofarm.support.experience.presentation;

import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.experience.application.ExperienceService;
import com.barofarm.support.experience.application.dto.ExperienceServiceResponse;
import com.barofarm.support.experience.presentation.dto.ExperienceRequest;
import com.barofarm.support.experience.presentation.dto.ExperienceResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 체험 프로그램 REST API 컨트롤러 */
@RestController
@RequiredArgsConstructor
public class ExperienceController implements ExperienceSwaggerApi {

    private final ExperienceService experienceService;

    @Override
    public ResponseDto<ExperienceResponse> createExperience(@Valid @RequestBody ExperienceRequest request) {
        // Command DTO → Service DTO 변환
        ExperienceServiceResponse serviceResponse = experienceService.createExperience(request.toServiceRequest());
        // Service DTO → Presentation DTO 변환
        return ResponseDto.ok(ExperienceResponse.from(serviceResponse));
    }

    @Override
    public ResponseDto<ExperienceResponse> getExperienceById(@PathVariable("id") UUID id) {
        System.out.println("=== ExperienceController.getExperienceById called with ID: " + id + " ===");
        ExperienceServiceResponse serviceResponse = experienceService.getExperienceById(id);
        return ResponseDto.ok(ExperienceResponse.from(serviceResponse));
    }

    @Override
    public ResponseDto<CustomPage<ExperienceResponse>> getExperiences(
        @RequestParam(required = false) UUID farmId,
        Pageable pageable) {
        System.out.println("=== ExperienceController.getExperiences called with farmId: " + farmId + " ===");

        var servicePage = farmId != null
            ? experienceService.getExperiencesByFarmId(farmId, pageable)
            : experienceService.getAllExperiences(pageable);

        var responsePage = servicePage.map(ExperienceResponse::from);
        return ResponseDto.ok(CustomPage.from(responsePage));
    }

    @Override
    public ResponseDto<ExperienceResponse> updateExperience(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ExperienceRequest request) {
        ExperienceServiceResponse serviceResponse = experienceService.updateExperience(id, request.toServiceRequest());
        return ResponseDto.ok(ExperienceResponse.from(serviceResponse));
    }

    @Override
    public ResponseDto<Void> deleteExperience(@PathVariable("id") UUID id) {
        experienceService.deleteExperience(id);
        return ResponseDto.ok(null);
    }
}
