package com.barofarm.support.search.experience.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Document(indexName = "experience")
public class ExperienceDocument {
    @Id
    private UUID experienceId; // PK

    @Field(type = FieldType.Text, analyzer = "nori")
    private String experienceName; // 체험명

    @Field(type = FieldType.Long)
    private Long pricePerPerson; // 1인당 가격 (필터링)

    @Field(type = FieldType.Integer)
    private Integer capacity; // 수용 인원 (필터링)

    @Field(type = FieldType.Integer)
    private Integer durationMinutes; // 소요 시간 (필터링)

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate availableStartDate; // 예약 가능 시작일 (필터링)

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate availableEndDate; // 예약 가능 종료일 (필터링)

    @Field(type = FieldType.Keyword)
    private String status; // 검색 결과 반환 로직 내에서 ON_SALE 상태만 결과에 노출

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    @JsonFormat(pattern = "uuuu-MM-dd'T'HH:mm:ssX", timezone = "UTC")
    private Instant updatedAt; // 검색 결과 반환 로직 내에서 최신순으로 정렬

    @Field(type = FieldType.Keyword)
    private String experienceNameChosung;

    public ExperienceDocument() {
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public ExperienceDocument(
        UUID experienceId,
        String experienceName,
        String experienceNameChosung,
        Long pricePerPerson,
        Integer capacity,
        Integer durationMinutes,
        LocalDate availableStartDate,
        LocalDate availableEndDate,
        String status,
        Instant updatedAt) {
        this.experienceId = experienceId;
        this.experienceName = experienceName;
        this.experienceNameChosung = experienceNameChosung;
        this.pricePerPerson = pricePerPerson;
        this.capacity = capacity;
        this.durationMinutes = durationMinutes;
        this.availableStartDate = availableStartDate;
        this.availableEndDate = availableEndDate;
        this.status = status;
        this.updatedAt = updatedAt;
    }
}
