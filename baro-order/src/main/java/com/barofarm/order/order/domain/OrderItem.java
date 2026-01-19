package com.barofarm.order.order.domain;

import com.barofarm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_item")
public class OrderItem extends BaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", columnDefinition = "BINARY(16)", nullable = false)
    private Order order;

    @Column(name = "product_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID productId;

    @Column(name = "email", nullable = false)
    private String productName;

    @Column(name = "seller_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID sellerId;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "unit_price", nullable = false)
    private Long unitPrice;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Column(name = "inventory_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID inventoryId;

    private OrderItem(UUID id, Order order, UUID productId, String productName,
                      UUID sellerId, Long quantity, Long unitPrice, UUID inventoryId) {
        this.id = id;
        this.order = order;
        this.productId = productId;
        this.productName = productName;
        this.sellerId = sellerId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice*quantity;
        this.inventoryId = inventoryId;
    }

    public static OrderItem of(Order order, UUID productId, String productName,
                               UUID sellerId, Long quantity, Long unitPrice, UUID inventoryId) {
        return new OrderItem(
            UUID.randomUUID(),
            order,
            productId,
            productName,
            sellerId,
            quantity,
            unitPrice,
            inventoryId
        );
    }
}
