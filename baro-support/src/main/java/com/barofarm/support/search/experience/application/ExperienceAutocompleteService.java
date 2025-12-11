package com.barofarm.support.search.experience.application;

import com.barofarm.support.search.experience.application.dto.ExperienceAutoItem;
import com.barofarm.support.search.experience.infrastructure.elasticsearch.ExperienceAutocompleteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExperienceAutocompleteService {

    private final ExperienceAutocompleteRepository repository;

    public List<ExperienceAutoItem> autocomplete(String query) {
        return repository.findByPrefix(query).stream()
            .map(document -> new ExperienceAutoItem(document.getExperienceId(), document.getExperienceName()))
            .distinct()
            .limit(5) // 자동완성 5개까지 출력
            .toList();
    }
}
