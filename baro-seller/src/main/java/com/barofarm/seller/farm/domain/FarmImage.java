package com.barofarm.seller.farm.domain;

import com.barofarm.seller.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "farm_image")
public class FarmImage extends BaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", columnDefinition = "BINARY(16)", nullable = false, unique = true)
    private Farm farm;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "s3_key", nullable = false, length = 500, unique = true)
    private String s3Key;

    private FarmImage(Farm farm, String url, String s3Key) {
        this.id = UUID.randomUUID();
        this.farm = farm;
        this.url = url;
        this.s3Key = s3Key;
    }

    public static FarmImage of(Farm farm, String url, String s3Key) {
        return new FarmImage(farm, url, s3Key);
    }
}
