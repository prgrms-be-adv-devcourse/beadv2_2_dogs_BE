package com.barofarm.buyer.product.domain;

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
@Table(name = "categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Category extends BaseEntity {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(nullable = false, length = 50)
  private String code;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Category parent;

  @Column(name = "level")
  private Integer level;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder;

  private Category(
      UUID id,
      String name,
      String code,
      Category parent,
      Integer level,
      Integer sortOrder) {
    this.id = id;
    this.name = name;
    this.code = code;
    this.parent = parent;
    this.level = level;
    this.sortOrder = sortOrder;
  }

  public static Category create(
      String name,
      String code,
      Category parent,
      Integer level,
      Integer sortOrder) {
    return new Category(UUID.randomUUID(), name, code, parent, level, sortOrder);
  }
}
