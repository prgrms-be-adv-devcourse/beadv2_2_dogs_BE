package com.barofarm.order.order.domain;

import com.barofarm.order.common.entity.BaseEntity;
import jakarta.persistence.*;
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

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private Long unitPrice;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    private OrderItem(UUID id, Order order, UUID productId, int quantity, Long unitPrice) {
        this.id = id;
        this.order = order;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice*quantity;
    }

    public static OrderItem of(Order order, UUID productId, int quantity, Long unitPrice) {
        return new OrderItem(
            UUID.randomUUID(),
            order,
            productId,
            quantity,
            unitPrice
        );
    }
}
