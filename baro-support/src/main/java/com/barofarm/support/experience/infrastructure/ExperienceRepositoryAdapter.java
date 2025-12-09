package com.barofarm.support.experience.infrastructure;

import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import java.util.List;
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
        System.out.println("=== ExperienceRepositoryAdapter.findById called with UUID: " + experienceId + " ===");
        Optional<Experience> result = jpaRepository.findById(experienceId);
        System.out.println("=== findById result: " + (result.isPresent() ? "FOUND - " + result.get().getTitle()
            : "NOT FOUND") + " ===");
        return result;
    }

    @Override
    public List<Experience> findByFarmId(UUID farmId) {
        return jpaRepository.findByFarmId(farmId);
    }

    @Override
    public List<Experience> findAll() {
        return jpaRepository.findAll();
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
