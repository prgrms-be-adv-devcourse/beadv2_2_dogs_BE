package com.barofarm.support.experience.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.barofarm.support.experience.application.ExperienceService;
import com.barofarm.support.experience.application.dto.ExperienceServiceResponse;
import com.barofarm.support.experience.presentation.dto.ExperienceRequest;
import com.barofarm.support.experience.presentation.dto.ExperienceResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** ExperienceController 유닛 테스트 */
@ExtendWith(MockitoExtension.class)
class ExperienceControllerTest {

    @Mock
    private ExperienceService experienceService;

    @InjectMocks
    private ExperienceController experienceController;

    private ExperienceRequest request;
    private ExperienceServiceResponse serviceResponse;

    @BeforeEach
    void setUp() {
        request = new ExperienceRequest(1L, "딸기 수확 체험", "신선한 딸기를 직접 수확해보세요", 15000, 20, LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 5, 31));

        serviceResponse = new ExperienceServiceResponse(1L, 1L, "딸기 수확 체험", "신선한 딸기를 직접 수확해보세요", 15000, 20,
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 31), LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /api/experiences - 체험 프로그램 생성")
    void createExperience() {
        // given
        when(experienceService.createExperience(any())).thenReturn(serviceResponse);

        // when
        ResponseEntity<ExperienceResponse> result = experienceController.createExperience(request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getTitle()).isEqualTo("딸기 수확 체험");
        verify(experienceService, times(1)).createExperience(any());
    }

    @Test
    @DisplayName("GET /api/experiences/{id} - ID로 체험 프로그램 조회")
    void getExperienceById() {
        // given
        when(experienceService.getExperienceById(1L)).thenReturn(serviceResponse);

        // when
        ResponseEntity<ExperienceResponse> result = experienceController.getExperienceById(1L);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getTitle()).isEqualTo("딸기 수확 체험");
        verify(experienceService, times(1)).getExperienceById(1L);
    }

    @Test
    @DisplayName("GET /api/experiences?farmId=1 - 농장 ID로 체험 프로그램 목록 조회")
    void getExperiencesByFarmId() {
        // given
        ExperienceServiceResponse serviceResponse2 = new ExperienceServiceResponse(2L, 1L, "블루베리 수확 체험", "달콤한 블루베리",
                20000, 15, LocalDate.of(2025, 6, 1), LocalDate.of(2025, 8, 31), LocalDateTime.now(),
                LocalDateTime.now());

        List<ExperienceServiceResponse> serviceResponses = Arrays.asList(serviceResponse, serviceResponse2);
        when(experienceService.getExperiencesByFarmId(1L)).thenReturn(serviceResponses);

        // when
        ResponseEntity<List<ExperienceResponse>> result = experienceController.getExperiences(1L);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(2);
        assertThat(result.getBody()).extracting("title").contains("딸기 수확 체험", "블루베리 수확 체험");
        verify(experienceService, times(1)).getExperiencesByFarmId(1L);
    }

    @Test
    @DisplayName("GET /api/experiences - 모든 체험 프로그램 조회")
    void getAllExperiences() {
        // given
        List<ExperienceServiceResponse> serviceResponses = Arrays.asList(serviceResponse);
        when(experienceService.getAllExperiences()).thenReturn(serviceResponses);

        // when
        ResponseEntity<List<ExperienceResponse>> result = experienceController.getExperiences(null);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
        assertThat(result.getBody().get(0).getTitle()).isEqualTo("딸기 수확 체험");
        verify(experienceService, times(1)).getAllExperiences();
    }

    @Test
    @DisplayName("PUT /api/experiences/{id} - 체험 프로그램 수정")
    void updateExperience() {
        // given
        ExperienceServiceResponse updatedServiceResponse = new ExperienceServiceResponse(1L, 1L, "수정된 제목", "수정된 설명",
                25000, 30, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 6, 30), LocalDateTime.now(),
                LocalDateTime.now());

        when(experienceService.updateExperience(eq(1L), any())).thenReturn(updatedServiceResponse);

        // when
        ResponseEntity<ExperienceResponse> result = experienceController.updateExperience(1L, request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getTitle()).isEqualTo("수정된 제목");
        assertThat(result.getBody().getPrice()).isEqualTo(25000);
        verify(experienceService, times(1)).updateExperience(eq(1L), any());
    }

    @Test
    @DisplayName("DELETE /api/experiences/{id} - 체험 프로그램 삭제")
    void deleteExperience() {
        // given
        doNothing().when(experienceService).deleteExperience(1L);

        // when
        ResponseEntity<Void> result = experienceController.deleteExperience(1L);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(experienceService, times(1)).deleteExperience(1L);
    }
}
