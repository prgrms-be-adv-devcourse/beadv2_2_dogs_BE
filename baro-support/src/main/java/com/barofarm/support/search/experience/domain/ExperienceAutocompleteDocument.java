package com.barofarm.support.search.experience.domain;

import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Getter
@Document(indexName = "experience-autocomplete")
@Setting(settingPath = "settings/autocomplete-analyzer.json")
public class ExperienceAutocompleteDocument {

    @Id
    private UUID experienceId;

    @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "standard")
    private String experienceName;

    public ExperienceAutocompleteDocument(UUID experienceId, String experienceName) {
        this.experienceId = experienceId;
        this.experienceName = experienceName;
    }
}
