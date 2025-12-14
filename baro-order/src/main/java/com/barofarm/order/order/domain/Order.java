package com.barofarm.order.order.domain;

import com.barofarm.order.common.entity.BaseEntity;
import com.barofarm.order.order.application.dto.request.OrderCreateCommand;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "zip_code", nullable = false)
    private String zipCode;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "address_detail", nullable = false)
    private String addressDetail;

    @Column(name = "delivery_memo")
    private String deliveryMemo;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @SuppressWarnings("checkstyle:ParameterNumber")
    private Order(
            UUID id,
            UUID userId,
            String receiverName,
            String phone,
            String email,
            String zipCode,
            String address,
            String addressDetail,
            String deliveryMemo
    ) {
        this.id = id;
        this.userId = userId;
        this.receiverName = receiverName;
        this.phone = phone;
        this.email = email;
        this.zipCode = zipCode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.deliveryMemo = deliveryMemo;
        this.totalAmount = 0L;
        this.status = OrderStatus.PENDING;
    }

    public static Order of(UUID userId, OrderCreateCommand command) {
        return new Order(
                UUID.randomUUID(),
                userId,
                command.receiverName(),
                command.phone(),
                command.email(),
                command.zipCode(),
                command.address(),
                command.addressDetail(),
                command.deliveryMemo()
        );
    }

    public void addOrderItem(UUID productId, UUID sellerId, int quantity, Long unitPrice) {
        OrderItem orderItem = OrderItem.of(this, productId, sellerId, quantity, unitPrice);
        this.orderItems.add(orderItem);
        this.totalAmount += orderItem.getTotalPrice();
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }

    public boolean isCanceled() {
        return this.status == OrderStatus.CANCELED;
    }
}
