package com.barofarm.support.search.farm.application;

import com.barofarm.support.search.farm.domain.FarmAutocompleteDocument;
import com.barofarm.support.search.farm.infrastructure.elasticsearch.FarmAutocompleteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FarmAutocompleteService {

    private final FarmAutocompleteRepository repository;

    public List<String> autocomplete(String query) {
        return repository.findByPrefix(query).stream()
            .map(FarmAutocompleteDocument::getFarmName)
            .distinct()
            .limit(5) // 자동완성 5개까지 출력
            .toList();
    }
}
