package com.barofarm.buyer.inventory.domain;

import static com.barofarm.buyer.inventory.exception.InventoryErrorCode.INSUFFICIENT_STOCK;
import static com.barofarm.buyer.inventory.exception.InventoryErrorCode.INVALID_REQUEST;

import com.barofarm.buyer.common.entity.BaseEntity;
import com.barofarm.buyer.common.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID productId;

    @Column(nullable = false)
    private int quantity;

    public void decrease(int amount) {
        if (amount <= 0) {
            throw new CustomException(INVALID_REQUEST);
        }
        if (this.quantity < amount) {
            throw new CustomException(INSUFFICIENT_STOCK);
        }
        this.quantity -= amount;
    }

    public void increase(int quantity) {
        this.quantity += quantity;
    }
}
