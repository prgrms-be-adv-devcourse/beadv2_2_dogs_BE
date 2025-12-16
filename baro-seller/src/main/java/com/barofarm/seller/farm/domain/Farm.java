package com.barofarm.seller.farm.domain;

import static jakarta.persistence.EnumType.STRING;

import com.barofarm.seller.common.entity.BaseEntity;
import com.barofarm.seller.seller.domain.Seller;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "farm")
public class Farm extends BaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String address;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private Integer establishedYear;

    @Column(nullable = false, length = 30)
    private String farmSize;

    @Column(nullable = false, length = 30)
    private String cultivationMethod;

    @Enumerated(STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", columnDefinition = "BINARY(16)")
    private Seller seller;

    @OneToOne(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true)
    private FarmImage image;

    private Farm(UUID id, Details details, Seller seller) {
        this.id = id;
        this.name = details.name();
        this.description = details.description();
        this.address = details.address();
        this.phone = details.phone();
        this.email = details.email();
        this.establishedYear = details.establishedYear();
        this.farmSize = details.farmSize();
        this.cultivationMethod = details.cultivationMethod();
        this.seller = seller;
    }

    public static Farm of(Details details, Seller seller) {
        return new Farm(UUID.randomUUID(), details, seller);
    }

    public void update(Details details) {
        this.name = details.name();
        this.description = details.description();
        this.address = details.address();
        this.phone = details.phone();
        this.email = details.email();
        this.establishedYear = details.establishedYear();
        this.farmSize = details.farmSize();
        this.cultivationMethod = details.cultivationMethod();
    }

    public record Details(
        String name,
        String description,
        String address,
        String phone,
        String email,
        Integer establishedYear,
        String farmSize,
        String cultivationMethod
    ) {}

    public void removeImage() {
        this.image = null;
    }

    public void setImage(String url, String s3Key) {
        this.image = FarmImage.of(this, url, s3Key);
    }
}
