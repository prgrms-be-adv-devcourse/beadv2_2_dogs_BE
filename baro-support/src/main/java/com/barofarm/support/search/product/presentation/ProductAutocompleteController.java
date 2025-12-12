package com.barofarm.support.search.product.presentation;

import com.barofarm.support.search.product.application.ProductAutocompleteService;
import com.barofarm.support.search.product.application.dto.ProductAutoItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.v1}/search/product/autocomplete")
@RequiredArgsConstructor
public class ProductAutocompleteController {

    private final ProductAutocompleteService service;

    @GetMapping
    public List<ProductAutoItem> autocomplete(@RequestParam String query) {
        return service.autocomplete(query);
    }
}
