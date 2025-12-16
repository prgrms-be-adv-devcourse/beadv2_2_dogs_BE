package com.barofarm.support.experience.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.experience.application.ExperienceService;
import com.barofarm.support.experience.application.dto.ExperienceServiceResponse;
import com.barofarm.support.experience.domain.ExperienceStatus;
import com.barofarm.support.experience.presentation.dto.ExperienceCreateRequest;
import com.barofarm.support.experience.presentation.dto.ExperienceResponse;
import com.barofarm.support.experience.presentation.dto.ExperienceUpdateRequest;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/** ExperienceController 유닛 테스트 */
@ExtendWith(MockitoExtension.class)
class ExperienceControllerTest {

    @Mock
    private ExperienceService experienceService;

    @InjectMocks
    private ExperienceController experienceController;

    private UUID farmId;
    private UUID experienceId;
    private UUID userId;
    private String userEmail;
    private String userRole;
    private ExperienceCreateRequest createRequest;
    private ExperienceUpdateRequest updateRequest;
    private ExperienceServiceResponse serviceResponse;

    @BeforeEach
    void setUp() {
        farmId = UUID.randomUUID();
        experienceId = UUID.randomUUID();
        userId = UUID.randomUUID();
        userEmail = "test@example.com";
        userRole = "SELLER";

        createRequest = new ExperienceCreateRequest(farmId, "딸기 수확 체험", "신선한 딸기를 직접 수확해보세요", BigInteger.valueOf(15000), 20,
                120, LocalDateTime.of(2025, 3, 1, 9, 0), LocalDateTime.of(2025, 5, 31, 18, 0), ExperienceStatus.ON_SALE);

        updateRequest = new ExperienceUpdateRequest("수정된 제목", "수정된 설명", BigInteger.valueOf(25000), 30, 150,
                LocalDateTime.of(2025, 4, 1, 9, 0), LocalDateTime.of(2025, 6, 30, 18, 0), ExperienceStatus.CLOSED);

        serviceResponse = new ExperienceServiceResponse(experienceId, farmId, "딸기 수확 체험", "신선한 딸기를 직접 수확해보세요",
                BigInteger.valueOf(15000), 20, 120, LocalDateTime.of(2025, 3, 1, 9, 0), LocalDateTime.of(2025, 5, 31, 18, 0),
                ExperienceStatus.ON_SALE, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /api/experiences - 체험 프로그램 생성")
    void createExperience() {
        when(experienceService.createExperience(eq(userId), any())).thenReturn(serviceResponse);

        ResponseDto<ExperienceResponse> result = experienceController.createExperience(userId, userEmail, userRole, createRequest);

        assertThat(result).isNotNull();
        assertThat(result.data()).isNotNull();
        assertThat(result.data().getTitle()).isEqualTo("딸기 수확 체험");
        verify(experienceService, times(1)).createExperience(eq(userId), any());
    }

    @Test
    @DisplayName("GET /api/experiences/{id} - ID로 체험 프로그램 조회")
    void getExperienceById() {
        // given
        when(experienceService.getExperienceById(experienceId)).thenReturn(serviceResponse);

        // when
        ResponseDto<ExperienceResponse> result = experienceController.getExperienceById(experienceId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isNotNull();
        assertThat(result.data().getTitle()).isEqualTo("딸기 수확 체험");
        verify(experienceService, times(1)).getExperienceById(experienceId);
    }

    @Test
    @DisplayName("GET /api/experiences?farmId=xxx - 농장 ID로 체험 프로그램 목록 조회")
    void getExperiencesByFarmId() {
        // given
        UUID experienceId2 = UUID.randomUUID();
        ExperienceServiceResponse serviceResponse2 = new ExperienceServiceResponse(experienceId2, farmId, "블루베리 수확 체험",
                "달콤한 블루베리", BigInteger.valueOf(20000), 15, 90, LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 8, 31, 18, 0),
                ExperienceStatus.ON_SALE, LocalDateTime.now(), LocalDateTime.now());

        Pageable pageable = PageRequest.of(0, 10);
        Page<ExperienceServiceResponse> servicePage = new PageImpl<>(
                java.util.Arrays.asList(serviceResponse, serviceResponse2), pageable, 2);
        when(experienceService.getExperiencesByFarmId(eq(farmId), any(Pageable.class))).thenReturn(servicePage);

        // when
        ResponseDto<CustomPage<ExperienceResponse>> result = experienceController.getExperiences(farmId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isNotNull();
        assertThat(result.data().content()).hasSize(2);
        assertThat(result.data().content()).extracting("title").contains("딸기 수확 체험", "블루베리 수확 체험");
        verify(experienceService, times(1)).getExperiencesByFarmId(eq(farmId), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/experiences - 모든 체험 프로그램 조회")
    void getAllExperiences() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ExperienceServiceResponse> servicePage = new PageImpl<>(
                java.util.Arrays.asList(serviceResponse), pageable, 1);
        when(experienceService.getAllExperiences(any(Pageable.class))).thenReturn(servicePage);

        // when
        ResponseDto<CustomPage<ExperienceResponse>> result = experienceController.getExperiences(null, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isNotNull();
        assertThat(result.data().content()).hasSize(1);
        assertThat(result.data().content().get(0).getTitle()).isEqualTo("딸기 수확 체험");
        verify(experienceService, times(1)).getAllExperiences(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/experiences/my-farm - 본인 또는 지정한 농장의 체험 프로그램 목록 조회")
    void getMyExperiences() {
        // given
        UUID userId = UUID.randomUUID();
        UUID customFarmId = farmId; // 선택적으로 전달되는 farmId
        UUID experienceId2 = UUID.randomUUID();
        ExperienceServiceResponse serviceResponse2 = new ExperienceServiceResponse(experienceId2, farmId, "블루베리 수확 체험",
                "달콤한 블루베리", BigInteger.valueOf(20000), 15, 90, LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 8, 31, 18, 0),
                ExperienceStatus.ON_SALE, LocalDateTime.now(), LocalDateTime.now());

        Pageable pageable = PageRequest.of(0, 10);
        Page<ExperienceServiceResponse> servicePage = new PageImpl<>(
                java.util.Arrays.asList(serviceResponse, serviceResponse2), pageable, 2);
        when(experienceService.getMyExperiences(eq(userId), eq(customFarmId), any(Pageable.class))).thenReturn(servicePage);

        // when
        ResponseDto<CustomPage<ExperienceResponse>> result =
                experienceController.getMyExperiences(userId, userEmail, userRole, customFarmId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isNotNull();
        assertThat(result.data().content()).hasSize(2);
        assertThat(result.data().content()).extracting("title").contains("딸기 수확 체험", "블루베리 수확 체험");
        verify(experienceService, times(1)).getMyExperiences(eq(userId), eq(customFarmId), any(Pageable.class));
    }

    @Test
    @DisplayName("PUT /api/experiences/{id} - 체험 프로그램 수정")
    void updateExperience() {
        // given
        ExperienceServiceResponse updatedServiceResponse = new ExperienceServiceResponse(experienceId, farmId, "수정된 제목",
                "수정된 설명", BigInteger.valueOf(25000), 30, 150, LocalDateTime.of(2025, 4, 1, 9, 0), LocalDateTime.of(2025, 6, 30, 18, 0),
                ExperienceStatus.CLOSED, LocalDateTime.now(), LocalDateTime.now());

        when(experienceService.updateExperience(eq(userId), eq(experienceId), any())).thenReturn(updatedServiceResponse);

        // when
        ResponseDto<ExperienceResponse> result = experienceController.updateExperience(userId, userEmail, userRole, experienceId, updateRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isNotNull();
        assertThat(result.data().getTitle()).isEqualTo("수정된 제목");
        assertThat(result.data().getPricePerPerson()).isEqualTo(BigInteger.valueOf(25000));
        verify(experienceService, times(1)).updateExperience(eq(userId), eq(experienceId), any());
    }

    @Test
    @DisplayName("DELETE /api/experiences/{id} - 체험 프로그램 삭제")
    void deleteExperience() {
        // given
        doNothing().when(experienceService).deleteExperience(userId, experienceId);

        // when
        ResponseDto<Void> result = experienceController.deleteExperience(userId, userEmail, userRole, experienceId);

        // then
        assertThat(result).isNotNull();
        verify(experienceService, times(1)).deleteExperience(userId, experienceId);
    }
}
