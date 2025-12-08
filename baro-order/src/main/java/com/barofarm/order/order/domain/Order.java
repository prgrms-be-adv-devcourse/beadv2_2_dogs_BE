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
@Table(name = "order")
public class Order extends BaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.CREATED;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;

    private Order(UUID id, UUID userId, Long totalAmount) {
        this.id = id;
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = Status.CREATED;
    }

    public static Order of(UUID userId, Long totalAmount) {
        return new Order(UUID.randomUUID(), userId, totalAmount);
    }
}
