package com.barofarm.support.search.product.application;

import com.barofarm.support.search.product.domain.ProductAutocompleteDocument;
import com.barofarm.support.search.product.infrastructure.elasticsearch.ProductAutocompleteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductAutocompleteService {

    private final ProductAutocompleteRepository repository;

    public List<String> autocomplete(String query) {
        return repository.findByPrefix(query).stream()
            .map(ProductAutocompleteDocument::getProductName)
            .distinct()
            .limit(5) // 자동완성 5개까지 출력
            .toList();
    }
}
