package com.barofarm.order.order.domain;

import com.barofarm.common.entity.BaseEntity;
import com.barofarm.order.order.application.dto.request.OrderCreateCommand;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @SuppressWarnings("checkstyle:ParameterNumber")
    private Order(
            UUID id,
            UUID userId,
            Address address
    ) {
        this.id = id;
        this.userId = userId;
        this.address = address;
        this.totalAmount = 0L;
        this.status = OrderStatus.PENDING;
    }

    public static Order of(UUID userId, OrderCreateCommand command) {
        return new Order(
                UUID.randomUUID(),
                userId,
                Address.from(command)
        );
    }

    public void addOrderItem(UUID productId, String productName, UUID sellerId,
                             Long quantity, Long unitPrice, UUID inventoryId) {
        OrderItem orderItem = OrderItem.of(this, productId, productName, sellerId, quantity, unitPrice, inventoryId);
        this.orderItems.add(orderItem);
        this.totalAmount += orderItem.getTotalPrice();
    }

    public void markFailed(){
        this.status = OrderStatus.FAILED;
    }

    public void markConfirmed(){
        this.status = OrderStatus.CONFIRMED;
    }

    public void markAwaitingPayment() {
        this.status = OrderStatus.AWAITING_PAYMENT;
    }

    public void markCancelPending() {
        this.status = OrderStatus.CANCEL_PENDING;
    }

    public void markCancel() {
        this.status = OrderStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }
}
