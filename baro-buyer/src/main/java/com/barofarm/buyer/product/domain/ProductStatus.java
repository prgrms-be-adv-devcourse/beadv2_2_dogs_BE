package com.barofarm.buyer.product.domain;

public enum ProductStatus {
  ON_SALE, // 판매중
  DISCOUNTED, // 할인중
  SOLD_OUT, // 재고 소진
  HIDDEN, // 비노출(검수)
  DELETED // 판매 중단
}
