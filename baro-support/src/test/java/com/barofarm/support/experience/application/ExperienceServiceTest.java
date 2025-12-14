package com.barofarm.support.experience.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barofarm.support.common.client.FarmClient;
import com.barofarm.support.experience.application.dto.ExperienceServiceRequest;
import com.barofarm.support.experience.application.dto.ExperienceServiceResponse;
import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import com.barofarm.support.experience.domain.ExperienceStatus;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
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

/** ExperienceService 유닛 테스트 */
@ExtendWith(MockitoExtension.class)
class ExperienceServiceTest {

    @Mock
    private ExperienceRepository experienceRepository;

    @Mock
    private FarmClient farmClient;

    @InjectMocks
    private ExperienceService experienceService;

    private UUID farmId;
    private UUID experienceId;
    private UUID userId;
    private ExperienceServiceRequest validRequest;
    private Experience validExperience;

    @BeforeEach
    void setUp() {
        farmId = UUID.randomUUID();
        experienceId = UUID.randomUUID();
        userId = UUID.randomUUID();

        validRequest = new ExperienceServiceRequest(farmId, "딸기 수확 체험", "신선한 딸기를 직접 수확해보세요",
                BigInteger.valueOf(15000), 20, 120, LocalDateTime.of(2025, 3, 1, 9, 0), LocalDateTime.of(2025, 5, 31, 18, 0),
                ExperienceStatus.ON_SALE);

        validExperience = new Experience(experienceId, farmId, "딸기 수확 체험", "신선한 딸기를 직접 수확해보세요",
                BigInteger.valueOf(15000), 20, 120, LocalDateTime.of(2025, 3, 1, 9, 0), LocalDateTime.of(2025, 5, 31, 18, 0),
                ExperienceStatus.ON_SALE);
    }

    @Test
    @DisplayName("유효한 체험 프로그램을 생성할 수 있다")
    void createExperience() {
        // given
        when(farmClient.getFarmIdByUserId(userId)).thenReturn(farmId);
        when(experienceRepository.save(any(Experience.class))).thenReturn(validExperience);

        // when
        ExperienceServiceResponse response = experienceService.createExperience(userId, validRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("딸기 수확 체험");
        verify(farmClient, times(1)).getFarmIdByUserId(userId);
        verify(experienceRepository, times(1)).save(any(Experience.class));
    }

    @Test
    @DisplayName("ID로 체험 프로그램을 조회할 수 있다")
    void getExperienceById() {
        // given
        when(experienceRepository.findById(experienceId)).thenReturn(Optional.of(validExperience));

        // when
        ExperienceServiceResponse response = experienceService.getExperienceById(experienceId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("딸기 수확 체험");
        verify(experienceRepository, times(1)).findById(experienceId);
    }

    @Test
    @DisplayName("농장 ID로 체험 프로그램 목록을 조회할 수 있다 (페이지네이션)")
    void getExperiencesByFarmId() {
        // given
        UUID experienceId2 = UUID.randomUUID();
        Experience experience2 = new Experience(experienceId2, farmId, "블루베리 수확 체험", "달콤한 블루베리",
                BigInteger.valueOf(20000), 15, 90, LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 8, 31, 18, 0),
                ExperienceStatus.ON_SALE);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Experience> experiencePage = new PageImpl<>(
                Arrays.asList(validExperience, experience2), pageable, 2);
        when(experienceRepository.findByFarmId(farmId, pageable)).thenReturn(experiencePage);

        // when
        Page<ExperienceServiceResponse> responsePage = experienceService.getExperiencesByFarmId(farmId, pageable);

        // then
        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getContent()).hasSize(2);
        assertThat(responsePage.getContent()).extracting("title").contains("딸기 수확 체험", "블루베리 수확 체험");
        verify(experienceRepository, times(1)).findByFarmId(farmId, pageable);
    }

    @Test
    @DisplayName("체험 프로그램을 수정할 수 있다")
    void updateExperience() {
        // given
        ExperienceServiceRequest updateRequest = new ExperienceServiceRequest(farmId, "수정된 제목", "수정된 설명",
                BigInteger.valueOf(25000), 30, 150, LocalDateTime.of(2025, 4, 1, 9, 0), LocalDateTime.of(2025, 6, 30, 18, 0),
                ExperienceStatus.CLOSED);

        when(experienceRepository.findById(experienceId)).thenReturn(Optional.of(validExperience));
        when(farmClient.getFarmIdByUserId(userId)).thenReturn(farmId);

        // when
        ExperienceServiceResponse response = experienceService.updateExperience(userId, experienceId, updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("수정된 제목");
        verify(farmClient, times(1)).getFarmIdByUserId(userId);
        verify(experienceRepository, times(1)).findById(experienceId);
        // JPA 더티 체킹 사용하므로 save 호출하지 않음
        verify(experienceRepository, never()).save(any(Experience.class));
    }

    @Test
    @DisplayName("체험 프로그램을 삭제할 수 있다")
    void deleteExperience() {
        // given
        when(experienceRepository.findById(experienceId)).thenReturn(Optional.of(validExperience));
        when(farmClient.getFarmIdByUserId(userId)).thenReturn(farmId);
        doNothing().when(experienceRepository).deleteById(experienceId);

        // when
        experienceService.deleteExperience(userId, experienceId);

        // then
        verify(farmClient, times(1)).getFarmIdByUserId(userId);
        verify(experienceRepository, times(1)).findById(experienceId);
        verify(experienceRepository, times(1)).deleteById(experienceId);
    }

    @Test
    @DisplayName("시작 날짜가 종료 날짜보다 늦으면 예외가 발생한다")
    void createExperienceInvalidDateRange() {
        // given
        ExperienceServiceRequest invalidRequest = new ExperienceServiceRequest(farmId, "딸기 수확 체험", "신선한 딸기를 직접 수확해보세요",
                BigInteger.valueOf(15000), 20, 120, LocalDateTime.of(2025, 5, 31, 18, 0), LocalDateTime.of(2025, 3, 1, 9, 0),
                ExperienceStatus.ON_SALE);
        when(farmClient.getFarmIdByUserId(userId)).thenReturn(farmId);

        // when & then
        assertThatThrownBy(() -> experienceService.createExperience(userId, invalidRequest))
                .isInstanceOf(com.barofarm.support.common.exception.CustomException.class)
                .satisfies(exception -> {
                    com.barofarm.support.common.exception.CustomException customException =
                            (com.barofarm.support.common.exception.CustomException) exception;
                    assertThat(customException.getErrorCode())
                            .isEqualTo(com.barofarm.support.experience.exception.ExperienceErrorCode.INVALID_DATE_RANGE);
                });

        verify(experienceRepository, never()).save(any(Experience.class));
    }

    @Test
    @DisplayName("사용자 ID로 본인 농장의 체험 프로그램 목록을 조회할 수 있다 (페이지네이션)")
    void getMyExperiences() {
        // given
        UUID userId = UUID.randomUUID();
        UUID experienceId2 = UUID.randomUUID();
        Experience experience2 = new Experience(experienceId2, farmId, "블루베리 수확 체험", "달콤한 블루베리",
                BigInteger.valueOf(20000), 15, 90, LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 8, 31, 18, 0),
                ExperienceStatus.ON_SALE);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Experience> experiencePage = new PageImpl<>(
                Arrays.asList(validExperience, experience2), pageable, 2);

        // FarmClient Mock: userId로 farmId 반환
        when(farmClient.getFarmIdByUserId(userId)).thenReturn(farmId);
        // ExperienceRepository Mock: farmId로 체험 목록 반환
        when(experienceRepository.findByFarmId(farmId, pageable)).thenReturn(experiencePage);

        // when
        Page<ExperienceServiceResponse> responsePage = experienceService.getMyExperiences(userId, pageable);

        // then
        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getContent()).hasSize(2);
        assertThat(responsePage.getContent()).extracting("title").contains("딸기 수확 체험", "블루베리 수확 체험");
        verify(farmClient, times(1)).getFarmIdByUserId(userId);
        verify(experienceRepository, times(1)).findByFarmId(farmId, pageable);
    }
}
