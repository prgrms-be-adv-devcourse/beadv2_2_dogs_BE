package com.barofarm.support.experience.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.barofarm.support.experience.application.dto.ExperienceServiceRequest;
import com.barofarm.support.experience.application.dto.ExperienceServiceResponse;
import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** ExperienceService 유닛 테스트 */
@ExtendWith(MockitoExtension.class)
class ExperienceServiceTest {

    @Mock
    private ExperienceRepository experienceRepository;

    @InjectMocks
    private ExperienceService experienceService;

    private ExperienceServiceRequest validRequest;
    private Experience validExperience;

    @BeforeEach
    void setUp() {
        validRequest = new ExperienceServiceRequest(1L, "딸기 수확 체험", "신선한 딸기를 직접 수확해보세요", 15000, 20,
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 31));

        validExperience = new Experience(1L, "딸기 수확 체험", "신선한 딸기를 직접 수확해보세요", 15000, 20, LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 5, 31));
        validExperience.setId(1L);
    }

    @Test
    @DisplayName("유효한 체험 프로그램을 생성할 수 있다")
    void createExperience() {
        // given
        when(experienceRepository.save(any(Experience.class))).thenReturn(validExperience);

        // when
        ExperienceServiceResponse response = experienceService.createExperience(validRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("딸기 수확 체험");
        verify(experienceRepository, times(1)).save(any(Experience.class));
    }

    @Test
    @DisplayName("ID로 체험 프로그램을 조회할 수 있다")
    void getExperienceById() {
        // given
        when(experienceRepository.findById(1L)).thenReturn(Optional.of(validExperience));

        // when
        ExperienceServiceResponse response = experienceService.getExperienceById(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("딸기 수확 체험");
        verify(experienceRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("농장 ID로 체험 프로그램 목록을 조회할 수 있다")
    void getExperiencesByFarmId() {
        // given
        Experience experience2 = new Experience(1L, "블루베리 수확 체험", "달콤한 블루베리", 20000, 15, LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 8, 31));

        when(experienceRepository.findByFarmId(1L)).thenReturn(Arrays.asList(validExperience, experience2));

        // when
        List<ExperienceServiceResponse> responses = experienceService.getExperiencesByFarmId(1L);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("title").contains("딸기 수확 체험", "블루베리 수확 체험");
        verify(experienceRepository, times(1)).findByFarmId(1L);
    }

    @Test
    @DisplayName("체험 프로그램을 수정할 수 있다")
    void updateExperience() {
        // given
        ExperienceServiceRequest updateRequest = new ExperienceServiceRequest(1L, "수정된 제목", "수정된 설명", 25000, 30,
                LocalDate.of(2025, 4, 1), LocalDate.of(2025, 6, 30));

        when(experienceRepository.findById(1L)).thenReturn(Optional.of(validExperience));
        when(experienceRepository.save(any(Experience.class))).thenReturn(validExperience);

        // when
        ExperienceServiceResponse response = experienceService.updateExperience(1L, updateRequest);

        // then
        assertThat(response).isNotNull();
        verify(experienceRepository, times(1)).findById(1L);
        verify(experienceRepository, times(1)).save(any(Experience.class));
    }

    @Test
    @DisplayName("체험 프로그램을 삭제할 수 있다")
    void deleteExperience() {
        // given
        when(experienceRepository.existsById(1L)).thenReturn(true);
        doNothing().when(experienceRepository).deleteById(1L);

        // when
        experienceService.deleteExperience(1L);

        // then
        verify(experienceRepository, times(1)).existsById(1L);
        verify(experienceRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("시작 날짜가 종료 날짜보다 늦으면 예외가 발생한다")
    void createExperienceInvalidDateRange() {
        // given
        ExperienceServiceRequest invalidRequest = new ExperienceServiceRequest(1L, "딸기 수확 체험", "신선한 딸기를 직접 수확해보세요",
                15000, 20, LocalDate.of(2025, 5, 31), LocalDate.of(2025, 3, 1));

        // when & then
        assertThatThrownBy(() -> experienceService.createExperience(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("시작 날짜는 종료 날짜보다 이전이어야 합니다");

        verify(experienceRepository, never()).save(any(Experience.class));
    }
}
