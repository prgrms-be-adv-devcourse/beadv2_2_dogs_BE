package com.barofarm.order.order.domain;

import com.barofarm.order.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(name = "address", nullable = false)
    private String address;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(UUID id, UUID userId, String address) {
        this.id = id;
        this.userId = userId;
        this.address = address;
        this.totalAmount = 0L;
        this.status = OrderStatus.CREATED;
    }

    public static Order of(UUID userId, String address) {
        return new Order(UUID.randomUUID(), userId, address);
    }

    public void addOrderItem(UUID productId, int quantity, Long unitPrice) {
        OrderItem orderItem = OrderItem.of(this, productId, quantity, unitPrice);
        this.orderItems.add(orderItem);
        this.totalAmount += orderItem.getTotalPrice();
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
    }

}
