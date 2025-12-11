package com.barofarm.support.search.experience.application;

import com.barofarm.support.search.experience.domain.ExperienceAutocompleteDocument;
import com.barofarm.support.search.experience.infrastructure.elasticsearch.ExperienceAutocompleteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExperienceAutocompleteService {

    private final ExperienceAutocompleteRepository repository;

    public List<String> autocomplete(String query) {
        return repository.findByPrefix(query).stream()
            .map(ExperienceAutocompleteDocument::getExperienceName)
            .distinct()
            .limit(5) // 자동완성 5개까지 출력
            .toList();
    }
}
