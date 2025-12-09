package com.barofarm.support.experience.domain;

import com.barofarm.support.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 체험 프로그램 엔티티 (FARM_EXPERIENCE) */
@Entity
@Table(name = "farm_experience")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Experience extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "experience_id", columnDefinition = "BINARY(16)")
    private UUID experienceId;

    @Column(name = "farm_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID farmId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_per_person", nullable = false)
    private BigInteger pricePerPerson;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "available_start_date", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime availableStartDate;

    @Column(name = "available_end_date", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime availableEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceStatus status;
}
