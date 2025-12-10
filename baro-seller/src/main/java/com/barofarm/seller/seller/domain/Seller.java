package com.barofarm.seller.seller.domain;

import com.barofarm.seller.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "seller")
public class Seller extends BaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "store_name", nullable = false, length = 50)
    private String storeName;

    @Column(name = "business_reg_no", nullable = false, length = 30)
    private String businessRegNo;

    @Column(name = "business_owner_name", nullable = false, length = 50)
    private String businessOwnerName;

    @Column(name = "settlement_bank", nullable = false, length = 30)
    private String settlementBank;

    @Column(name = "settlement_account", nullable = false, length = 50)
    private String settlementAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "seller_status", nullable = false, length = 20)
    private Status status;

    public boolean isActive() {
        return this.status == Status.APPROVED;
    }

    private Seller(UUID id,
                   String storeName,
                   String businessRegNo,
                   String businessOwnerName,
                   String settlementBank,
                   String settlementAccount,
                   Status status) {

        this.id = id;
        this.storeName = storeName;
        this.businessRegNo = businessRegNo;
        this.businessOwnerName = businessOwnerName;
        this.settlementBank = settlementBank;
        this.settlementAccount = settlementAccount;
        this.status = status;
    }

    public static Seller of(String storeName, String businessRegNo, String businessOwnerName, String settlementBank, String settlementAccount) {
        return new Seller(
            UUID.randomUUID(),
            storeName,
            businessRegNo,
            businessOwnerName,
            settlementBank,
            settlementAccount,
            Status.APPROVED
        );
    }
}
