package com.barofarm.support.experience.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    @DisplayName("농장 ID로 체험 프로그램 목록을 조회할 수 있다 (페이지네이션)")
    void findByFarmId() {
        // given
        UUID farmId2 = UUID.randomUUID();
        UUID experienceId3 = UUID.randomUUID();
        Experience experience3 = new Experience(experienceId3, farmId2, "감자 수확 체험", "감자 캐기 체험",
                BigInteger.valueOf(10000), 30, 60, LocalDateTime.of(2025, 9, 1, 9, 0), LocalDateTime.of(2025, 11, 30, 18, 0),
                ExperienceStatus.ON_SALE);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Experience> farm1Page = new PageImpl<>(Arrays.asList(experience1, experience2), pageable, 2);
        Page<Experience> farm2Page = new PageImpl<>(Arrays.asList(experience3), pageable, 1);

        when(experienceRepository.findByFarmId(farmId, pageable)).thenReturn(farm1Page);
        when(experienceRepository.findByFarmId(farmId2, pageable)).thenReturn(farm2Page);

        // when
        Page<Experience> farm1Experiences = experienceRepository.findByFarmId(farmId, pageable);
        Page<Experience> farm2Experiences = experienceRepository.findByFarmId(farmId2, pageable);

        // then
        assertThat(farm1Experiences.getContent()).hasSize(2);
        assertThat(farm1Experiences.getContent()).extracting("title").contains("딸기 수확 체험", "블루베리 수확 체험");

        assertThat(farm2Experiences.getContent()).hasSize(1);
        assertThat(farm2Experiences.getContent().get(0).getTitle()).isEqualTo("감자 수확 체험");
    }

    @Test
    @DisplayName("존재하지 않는 농장 ID로 조회하면 빈 페이지를 반환한다")
    void findByFarmId_NotExists() {
        // given
        UUID notExistsFarmId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Experience> emptyPage = Page.empty(pageable);
        when(experienceRepository.findByFarmId(notExistsFarmId, pageable)).thenReturn(emptyPage);

        // when
        Page<Experience> experiences = experienceRepository.findByFarmId(notExistsFarmId, pageable);

        // then
        assertThat(experiences.getContent()).isEmpty();
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
    @DisplayName("모든 체험 프로그램을 조회할 수 있다 (페이지네이션)")
    void findAll() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Experience> allPage = new PageImpl<>(Arrays.asList(experience1, experience2), pageable, 2);
        when(experienceRepository.findAll(pageable)).thenReturn(allPage);

        // when
        Page<Experience> all = experienceRepository.findAll(pageable);

        // then
        assertThat(all.getContent()).hasSize(2);
        verify(experienceRepository, times(1)).findAll(pageable);
    }
}
