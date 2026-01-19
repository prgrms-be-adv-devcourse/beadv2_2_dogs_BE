package com.barofarm.buyer.inventory.domain;

import static com.barofarm.buyer.inventory.exception.InventoryErrorCode.ALREADY_CANCELED;
import static com.barofarm.buyer.inventory.exception.InventoryErrorCode.ALREADY_CONFIRMED;

import com.barofarm.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "inventory_reservation",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order_id", "inventory_id"})
    }
)
public class InventoryReservation {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "order_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", columnDefinition = "BINARY(16)", nullable = false)
    private Inventory inventory;

    @Column(nullable = false)
    private Long reservedQuantity;

    @Enumerated(EnumType.STRING)
    private InventoryReservationStatus inventoryReservationStatus;

    private InventoryReservation(
        UUID id,
        UUID orderId,
        Long quantity,
        InventoryReservationStatus inventoryReservationStatus
    ) {
        this.id = id;
        this.orderId = orderId;
        this.reservedQuantity = quantity;
        this.inventoryReservationStatus = inventoryReservationStatus;
    }

    public static InventoryReservation of(UUID orderId, Long reservedQuantity) {
        return new InventoryReservation(
            UUID.randomUUID(),
            orderId,
            reservedQuantity,
            InventoryReservationStatus.RESERVED
        );
    }

    public void confirm() {
        if (this.inventoryReservationStatus == InventoryReservationStatus.CANCELED) {
            throw new CustomException(ALREADY_CANCELED);
        }
        this.inventoryReservationStatus = InventoryReservationStatus.CONFIRMED;
    }

    public void markCancel() {
        if (this.inventoryReservationStatus == InventoryReservationStatus.CONFIRMED) {
            throw new CustomException(ALREADY_CONFIRMED);
        }
        this.inventoryReservationStatus = InventoryReservationStatus.CANCELED;
    }

    public void assignInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
