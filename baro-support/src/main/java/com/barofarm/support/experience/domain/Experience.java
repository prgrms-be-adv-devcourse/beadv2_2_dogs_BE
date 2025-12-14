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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** 체험 프로그램 엔티티 */
@Entity
@Table(name = "farm_experience")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
// Set/Map 사용 시 동일성 비교를 위해 ID만 사용 (모든 필드 포함 시 영속성 컨텍스트 이슈 발생 가능)
@EqualsAndHashCode(callSuper = false, of = "experienceId")
@ToString(callSuper = false)
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

    /**
     * 체험 프로그램 정보 업데이트
     *
     * @param title 체험 제목
     * @param description 체험 설명
     * @param pricePerPerson 1인당 가격
     * @param capacity 수용 인원
     * @param durationMinutes 소요 시간 (분)
     * @param availableStartDate 예약 가능 시작일
     * @param availableEndDate 예약 가능 종료일
     * @param status 체험 상태
     */
    public void update(
            String title,
            String description,
            BigInteger pricePerPerson,
            Integer capacity,
            Integer durationMinutes,
            LocalDateTime availableStartDate,
            LocalDateTime availableEndDate,
            ExperienceStatus status
    ) {
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (pricePerPerson != null) {
            this.pricePerPerson = pricePerPerson;
        }
        if (capacity != null) {
            this.capacity = capacity;
        }
        if (durationMinutes != null) {
            this.durationMinutes = durationMinutes;
        }
        if (availableStartDate != null) {
            this.availableStartDate = availableStartDate;
        }
        if (availableEndDate != null) {
            this.availableEndDate = availableEndDate;
        }
        if (status != null) {
            this.status = status;
        }
    }
}
