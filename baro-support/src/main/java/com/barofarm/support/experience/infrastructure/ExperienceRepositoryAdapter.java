package com.barofarm.support.experience.infrastructure;

import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/** 체험 프로그램 리포지토리 어댑터 (Adapter) */
@Repository
@RequiredArgsConstructor
public class ExperienceRepositoryAdapter implements ExperienceRepository {

    private final ExperienceJpaRepository jpaRepository;

    @Override
    public Experience save(Experience experience) {
        return jpaRepository.save(experience);
    }

    @Override
    public Optional<Experience> findById(UUID experienceId) {
        return jpaRepository.findById(experienceId);
    }

    @Override
    public boolean existsById(UUID experienceId) {
        return jpaRepository.existsById(experienceId);
    }

    @Override
    public void deleteById(UUID experienceId) {
        jpaRepository.deleteById(experienceId);
    }

    @Override
    public boolean existsByFarmIdAndExperienceId(UUID farmId, UUID experienceId) {
        return jpaRepository.existsByFarmIdAndExperienceId(farmId, experienceId);
    }

    @Override
    public Page<Experience> findByFarmId(UUID farmId, Pageable pageable) {
        return jpaRepository.findByFarmId(farmId, pageable);
    }

    @Override
    public Page<Experience> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }
}
