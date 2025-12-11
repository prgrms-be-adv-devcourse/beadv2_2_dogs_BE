package com.barofarm.support.search.farm.presentation;

import com.barofarm.support.search.farm.application.FarmAutocompleteService;
import com.barofarm.support.search.farm.application.dto.FarmAutoItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search/farm/autocomplete")
@RequiredArgsConstructor
public class FarmAutocompleteController {

    private final FarmAutocompleteService service;

    @GetMapping
    public List<FarmAutoItem> autocomplete(@RequestParam String query) {
        return service.autocomplete(query);
    }
}
