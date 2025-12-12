package com.barofarm.support.search.experience.presentation;

import com.barofarm.support.search.experience.application.ExperienceAutocompleteService;
import com.barofarm.support.search.experience.application.dto.ExperienceAutoItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.v1}/search/experience/autocomplete")
@RequiredArgsConstructor
public class ExperienceAutocompleteController {

    private final ExperienceAutocompleteService service;

    @GetMapping
    public List<ExperienceAutoItem> autocomplete(@RequestParam String query) {
        return service.autocomplete(query);
    }
}
