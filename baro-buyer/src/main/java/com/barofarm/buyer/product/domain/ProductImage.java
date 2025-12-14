package com.barofarm.buyer.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductImage {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    private ProductImage(Product product, String imageUrl, Integer sortOrder) {
        this.id = UUID.randomUUID();
        this.product = product;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public static ProductImage create(Product product, String imageUrl, Integer sortOrder){
        return new ProductImage(product, imageUrl, sortOrder);
    }
}
