package com.barofarm.seller.farm.domain;

import com.barofarm.seller.common.entity.BaseEntity;
import com.barofarm.seller.seller.domain.Seller;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", columnDefinition = "BINARY(16)")
    private Seller seller;

    @OneToOne(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true)
    private FarmImage image;

    private Farm(UUID id, String name, String description, String address, String phone,
                 String email, Integer establishedYear, String farmSize, String cultivationMethod,
                 Seller seller) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.establishedYear = establishedYear;
        this.farmSize = farmSize;
        this.cultivationMethod = cultivationMethod;
        this.seller = seller;
    }

    public static Farm of(String name, String description, String address, String phone,
                          String email, Integer establishedYear, String farmSize, String cultivationMethod,
                          Seller seller) {
        return new Farm(UUID.randomUUID(), name, description, address, phone,
            email, establishedYear, farmSize, cultivationMethod, seller);
    }

    public void setImage(String url, String s3Key) {
        this.image = FarmImage.of(this, url, s3Key);
    }

    public void update(String name, String description, String address, String phone,
                       String email, Integer establishedYear, String farmSize, String cultivationMethod) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.establishedYear = establishedYear;
        this.farmSize = farmSize;
        this.cultivationMethod = cultivationMethod;
    }

    public void removeImage() {
        this.image = null;
    }
}
