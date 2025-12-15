package com.barofarm.support.delivery.domain;

import com.barofarm.support.common.entity.BaseEntity;
import com.barofarm.support.delivery.exception.InvalidDeliveryStatusException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Delivery extends BaseEntity {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "order_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID orderId;

    @Embedded
    private Address address;

    @Column(length = 50)
    private String courier;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private DeliveryStatus deliveryStatus;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    private Delivery(UUID orderId, Address address, String courier, String trackingNumber,
                    DeliveryStatus deliveryStatus, LocalDateTime shippedAt, LocalDateTime deliveredAt) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.address = address;
        this.courier = courier;
        this.trackingNumber = trackingNumber;
        this.deliveryStatus = deliveryStatus;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
    }

    public static Delivery create(UUID orderId, Address address) {
        return new Delivery(
            orderId,
            address,
            null,
            null,
            DeliveryStatus.READY,
            null,
            null
        );
    }

    public void ship(String courier, String trackingNumber) {
        validateStatusTransition(DeliveryStatus.READY);

        if (courier == null || courier.isBlank()) {
            throw new IllegalArgumentException("택배사는 필수입니다.");
        }
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("운송장 번호는 필수입니다.");
        }

        this.courier = courier;
        this.trackingNumber = trackingNumber;
        this.deliveryStatus = DeliveryStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
    }

    public void startDelivery() {
        validateStatusTransition(DeliveryStatus.SHIPPED);

        this.deliveryStatus = DeliveryStatus.DELIVERING;
    }

    public void completeDelivery() {
        validateStatusTransition(DeliveryStatus.DELIVERING);

        this.deliveryStatus = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    private void validateStatusTransition(DeliveryStatus target) {
        if (!this.deliveryStatus.canTransitionTo(target)) {
            throw new InvalidDeliveryStatusException(this.deliveryStatus);
        }
    }
}
