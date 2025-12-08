package com.barofarm.seller.farm.domain;

import com.barofarm.seller.common.entity.BaseEntity;
import com.barofarm.seller.seller.domain.Seller;
import jakarta.persistence.*;
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

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "address", nullable = false, length = 50)
    private String address;

    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", columnDefinition = "BINARY(16)")
    private Seller seller;

    private Farm(
        UUID id,
        String name,
        String description,
        String address,
        String phone,
        Seller seller
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.seller = seller;
        this.status = Status.ACTIVE;
    }

    public static Farm of(
        String name,
        String description,
        String address,
        String phone,
        Seller seller
    ) {
        return new Farm(UUID.randomUUID(), name, description, address, phone, seller);
    }

    public void update(
        String name,
        String description,
        String address,
        String phone
    ) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
    }
}
