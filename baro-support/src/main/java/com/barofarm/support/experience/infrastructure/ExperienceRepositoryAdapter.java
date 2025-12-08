package com.barofarm.support.experience.infrastructure;

import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
    public Optional<Experience> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Experience> findByFarmId(Long farmId) {
        return jpaRepository.findByFarmId(farmId);
    }

    @Override
    public List<Experience> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByFarmIdAndId(Long farmId, Long id) {
        return jpaRepository.existsByFarmIdAndId(farmId, id);
    }
}
