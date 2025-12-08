package com.barofarm.support.experience.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** ExperienceRepository 유닛 테스트 */
@ExtendWith(MockitoExtension.class)
class ExperienceRepositoryTest {

    @Mock
    private ExperienceRepository experienceRepository;

    private Experience experience1;
    private Experience experience2;

    @BeforeEach
    void setUp() {
        experience1 = new Experience(1L, "딸기 수확 체험", "신선한 딸기를 직접 수확해보세요", 15000, 20, LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 5, 31));
        experience1.setId(1L);

        experience2 = new Experience(1L, "블루베리 수확 체험", "달콤한 블루베리를 수확해보세요", 20000, 15, LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 8, 31));
        experience2.setId(2L);
    }

    @Test
    @DisplayName("체험 프로그램을 저장하고 조회할 수 있다")
    void saveAndFindById() {
        // given
        when(experienceRepository.save(any(Experience.class))).thenReturn(experience1);
        when(experienceRepository.findById(1L)).thenReturn(Optional.of(experience1));

        // when
        Experience saved = experienceRepository.save(experience1);
        Optional<Experience> found = experienceRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("딸기 수확 체험");
        assertThat(found.get().getFarmId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("농장 ID로 체험 프로그램 목록을 조회할 수 있다")
    void findByFarmId() {
        // given
        Experience experience3 = new Experience(2L, "감자 수확 체험", "감자 캐기 체험", 10000, 30, LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 11, 30));
        experience3.setId(3L);

        when(experienceRepository.findByFarmId(1L)).thenReturn(Arrays.asList(experience1, experience2));
        when(experienceRepository.findByFarmId(2L)).thenReturn(Arrays.asList(experience3));

        // when
        List<Experience> farm1Experiences = experienceRepository.findByFarmId(1L);
        List<Experience> farm2Experiences = experienceRepository.findByFarmId(2L);

        // then
        assertThat(farm1Experiences).hasSize(2);
        assertThat(farm1Experiences).extracting("title").contains("딸기 수확 체험", "블루베리 수확 체험");

        assertThat(farm2Experiences).hasSize(1);
        assertThat(farm2Experiences.get(0).getTitle()).isEqualTo("감자 수확 체험");
    }

    @Test
    @DisplayName("존재하지 않는 농장 ID로 조회하면 빈 목록을 반환한다")
    void findByFarmId_NotExists() {
        // given
        when(experienceRepository.findByFarmId(999L)).thenReturn(Arrays.asList());

        // when
        List<Experience> experiences = experienceRepository.findByFarmId(999L);

        // then
        assertThat(experiences).isEmpty();
    }

    @Test
    @DisplayName("농장 ID와 체험 ID로 존재 여부를 확인할 수 있다")
    void existsByFarmIdAndId() {
        // given
        when(experienceRepository.existsByFarmIdAndId(1L, 1L)).thenReturn(true);
        when(experienceRepository.existsByFarmIdAndId(2L, 1L)).thenReturn(false);

        // when
        boolean exists = experienceRepository.existsByFarmIdAndId(1L, 1L);
        boolean notExists = experienceRepository.existsByFarmIdAndId(2L, 1L);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("체험 프로그램을 삭제할 수 있다")
    void delete() {
        // given
        doNothing().when(experienceRepository).deleteById(1L);
        when(experienceRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        experienceRepository.deleteById(1L);

        // then
        Optional<Experience> found = experienceRepository.findById(1L);
        assertThat(found).isEmpty();
        verify(experienceRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("모든 체험 프로그램을 조회할 수 있다")
    void findAll() {
        // given
        when(experienceRepository.findAll()).thenReturn(Arrays.asList(experience1, experience2));

        // when
        List<Experience> all = experienceRepository.findAll();

        // then
        assertThat(all).hasSize(2);
        verify(experienceRepository, times(1)).findAll();
    }
}
