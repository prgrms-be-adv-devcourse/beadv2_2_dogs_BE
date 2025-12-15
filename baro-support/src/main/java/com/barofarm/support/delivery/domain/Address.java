package com.barofarm.support.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Column(name = "receiver_name", length = 30, nullable = false)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20, nullable = false)
    private String receiverPhone;

    @Column(name = "postal_code", length = 10, nullable = false)
    private String postalCode;

    @Column(name = "address_line1", length = 100, nullable = false)
    private String addressLine1;

    @Column(name = "address_line2", length = 100)
    private String addressLine2;

    public Address(
        String receiverName,
        String receiverPhone,
        String postalCode,
        String addressLine1,
        String addressLine2
    ) {
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.postalCode = postalCode;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
    }
}
