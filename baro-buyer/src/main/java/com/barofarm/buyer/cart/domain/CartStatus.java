package com.barofarm.buyer.cart.domain;

// 장바구니 상태
public enum CartStatus {
  ACTIVE,   // 활성 상태
  MERGED,   // 병합됨 (비로그인 → 로그인)
  EXPIRED   // 만료됨
}
