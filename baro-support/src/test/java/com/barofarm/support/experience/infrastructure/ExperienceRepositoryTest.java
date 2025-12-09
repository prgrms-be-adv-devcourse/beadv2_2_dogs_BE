package com.barofarm.support.experience.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import com.barofarm.support.experience.domain.ExperienceStatus;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    private UUID farmId;
    private UUID experienceId1;
    private UUID experienceId2;
    private Experience experience1;
    private Experience experience2;

    @BeforeEach
    void setUp() {
        farmId = UUID.randomUUID();
        experienceId1 = UUID.randomUUID();
        experienceId2 = UUID.randomUUID();

        experience1 = new Experience(experienceId1, farmId, "딸기 수확 체험", "신선한 딸기를 직접 수확해보세요",
                BigInteger.valueOf(15000), 20, 120, LocalDateTime.of(2025, 3, 1, 9, 0), LocalDateTime.of(2025, 5, 31, 18, 0),
                ExperienceStatus.ON_SALE);

        experience2 = new Experience(experienceId2, farmId, "블루베리 수확 체험", "달콤한 블루베리를 수확해보세요",
                BigInteger.valueOf(20000), 15, 90, LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 8, 31, 18, 0),
                ExperienceStatus.ON_SALE);
    }

    @Test
    @DisplayName("체험 프로그램을 저장하고 조회할 수 있다")
    void saveAndFindById() {
        // given
        when(experienceRepository.save(any(Experience.class))).thenReturn(experience1);
        when(experienceRepository.findById(experienceId1)).thenReturn(Optional.of(experience1));

        // when
        Experience saved = experienceRepository.save(experience1);
        Optional<Experience> found = experienceRepository.findById(saved.getExperienceId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("딸기 수확 체험");
        assertThat(found.get().getFarmId()).isEqualTo(farmId);
    }

    @Test
    @DisplayName("농장 ID로 체험 프로그램 목록을 조회할 수 있다")
    void findByFarmId() {
        // given
        UUID farmId2 = UUID.randomUUID();
        UUID experienceId3 = UUID.randomUUID();
        Experience experience3 = new Experience(experienceId3, farmId2, "감자 수확 체험", "감자 캐기 체험",
                BigInteger.valueOf(10000), 30, 60, LocalDateTime.of(2025, 9, 1, 9, 0), LocalDateTime.of(2025, 11, 30, 18, 0),
                ExperienceStatus.ON_SALE);

        when(experienceRepository.findByFarmId(farmId)).thenReturn(Arrays.asList(experience1, experience2));
        when(experienceRepository.findByFarmId(farmId2)).thenReturn(Arrays.asList(experience3));

        // when
        List<Experience> farm1Experiences = experienceRepository.findByFarmId(farmId);
        List<Experience> farm2Experiences = experienceRepository.findByFarmId(farmId2);

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
        UUID notExistsFarmId = UUID.randomUUID();
        when(experienceRepository.findByFarmId(notExistsFarmId)).thenReturn(Arrays.asList());

        // when
        List<Experience> experiences = experienceRepository.findByFarmId(notExistsFarmId);

        // then
        assertThat(experiences).isEmpty();
    }

    @Test
    @DisplayName("농장 ID와 체험 ID로 존재 여부를 확인할 수 있다")
    void existsByFarmIdAndExperienceId() {
        // given
        when(experienceRepository.existsByFarmIdAndExperienceId(farmId, experienceId1)).thenReturn(true);
        UUID otherFarmId = UUID.randomUUID();
        when(experienceRepository.existsByFarmIdAndExperienceId(otherFarmId, experienceId1)).thenReturn(false);

        // when
        boolean exists = experienceRepository.existsByFarmIdAndExperienceId(farmId, experienceId1);
        boolean notExists = experienceRepository.existsByFarmIdAndExperienceId(otherFarmId, experienceId1);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("체험 프로그램을 삭제할 수 있다")
    void delete() {
        // given
        doNothing().when(experienceRepository).deleteById(experienceId1);
        when(experienceRepository.findById(experienceId1)).thenReturn(Optional.empty());

        // when
        experienceRepository.deleteById(experienceId1);

        // then
        Optional<Experience> found = experienceRepository.findById(experienceId1);
        assertThat(found).isEmpty();
        verify(experienceRepository, times(1)).deleteById(experienceId1);
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
