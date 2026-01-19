package com.barofarm.buyer.inventory.domain;

import static com.barofarm.buyer.inventory.exception.InventoryErrorCode.INSUFFICIENT_STOCK;
import static com.barofarm.buyer.inventory.exception.InventoryErrorCode.INVALID_REQUEST;
import static com.barofarm.buyer.inventory.exception.InventoryErrorCode.RESERVED_QUANTITY_NOT_ENOUGH;

import com.barofarm.common.entity.BaseEntity;
import com.barofarm.exception.CustomException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inventory")
public class Inventory extends BaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, columnDefinition = "BINARY(16)")
    private UUID productId;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private Long reservedQuantity;

    @Column(name = "unit", nullable = false, length = 20)
    private Integer unit;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryReservation> inventoryReservations = new ArrayList<>();

    @Version
    private Long version;

    private Inventory(UUID id, UUID productId, Long quantity, Integer unit) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.reservedQuantity = 0L;
        this.unit = unit;
    }

    public static Inventory of(UUID productId, Long quantity, Integer unit) {
        return new Inventory(UUID.randomUUID(), productId, quantity, unit);
    }

    public void markReserve(Long requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity <= 0) {
            throw new CustomException(INVALID_REQUEST);
        }
        long reservableQuantity = this.quantity - this.reservedQuantity;
        if (reservableQuantity < requestedQuantity) {
            throw new CustomException(INSUFFICIENT_STOCK);
        }
        this.reservedQuantity += requestedQuantity;
    }

    public void confirm(Long requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity <= 0) {
            throw new CustomException(INVALID_REQUEST);
        }

        if (this.quantity < requestedQuantity) {
            throw new CustomException(INSUFFICIENT_STOCK);
        }

        if (this.reservedQuantity < requestedQuantity) {
            throw new CustomException(RESERVED_QUANTITY_NOT_ENOUGH);
        }

        this.quantity -= requestedQuantity;
        this.reservedQuantity -= requestedQuantity;
    }

    public void markCancel(Long requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity <= 0) {
            throw new CustomException(INVALID_REQUEST);
        }

        if (this.reservedQuantity < requestedQuantity) {
            throw new CustomException(RESERVED_QUANTITY_NOT_ENOUGH);
        }

        this.reservedQuantity -= requestedQuantity;
    }

    public void addInventoryReservation(InventoryReservation inventoryReservation) {
        this.inventoryReservations.add(inventoryReservation);
        inventoryReservation.assignInventory(this);
    }
}
