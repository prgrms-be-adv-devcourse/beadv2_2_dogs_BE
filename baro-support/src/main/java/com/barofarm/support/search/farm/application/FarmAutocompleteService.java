package com.barofarm.support.search.farm.application;

import com.barofarm.support.search.farm.application.dto.FarmAutoItem;
import com.barofarm.support.search.farm.infrastructure.elasticsearch.FarmAutocompleteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FarmAutocompleteService {

    private final FarmAutocompleteRepository repository;

    public List<FarmAutoItem> autocomplete(String query) {
        return repository.findByPrefix(query).stream()
            .map(document -> new FarmAutoItem(document.getFarmId(), document.getFarmName()))
            .distinct()
            .limit(5) // 자동완성 5개까지 출력
            .toList();
    }
}
