package com.barofarm.support.search.farm.domain;

import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Document(indexName = "farm-autocomplete")
public class FarmAutocompleteDocument {

    @Id
    private UUID farmId;

    @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "standard")
    private String farmName;

    public FarmAutocompleteDocument(UUID farmId, String farmName) {
        this.farmId = farmId;
        this.farmName = farmName;
    }
}
