package com.barofarm.buyer.product.exception;

import com.barofarm.buyer.common.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements BaseErrorCode {
  SELLER_NULL(HttpStatus.BAD_REQUEST, "seller id는 null이면 안됩니다"),

  // ======== 상품명 관련 ========
  PRODUCT_NAME_NULL(HttpStatus.BAD_REQUEST, "상품명은 필수 값입니다."),
  PRODUCT_NAME_EMPTY(HttpStatus.BAD_REQUEST, "상품명은 비어 있을 수 없습니다."),
  PRODUCT_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "상품명은 50자를 초과할 수 없습니다."),

  // ======== 카테고리 관련 ========
  CATEGORY_NULL(HttpStatus.BAD_REQUEST, "상품 카테고리는 필수 값입니다."),

  // ======== 가격 관련 ========
  PRICE_NULL(HttpStatus.BAD_REQUEST, "가격은 필수 값입니다."),
  INVALID_PRICE(HttpStatus.BAD_REQUEST, "가격은 0원 이상이어야 합니다."),
  PRICE_TOO_HIGH(HttpStatus.BAD_REQUEST, "가격이 허용 가능한 최대값을 초과했습니다."),

  // ======== 재고 관련 ========
  STOCK_NULL(HttpStatus.BAD_REQUEST, "재고 수량은 필수 값입니다."),
  INVALID_STOCK(HttpStatus.BAD_REQUEST, "재고 수량은 0 이상이어야 합니다."),
  STOCK_NEGATIVE(HttpStatus.BAD_REQUEST, "재고가 음수일 수 없습니다."),
  OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),

  // ======== 상태 관련 ========
  STATUS_NULL(HttpStatus.BAD_REQUEST, "상품 상태는 필수 값입니다."),
  INVALID_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 상품 상태입니다."),
  PRODUCT_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "해당 상품은 현재 판매 중이 아닙니다."),

  // ======== 권한 관련 ========
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
  FORBIDDEN_NOT_PRODUCT_OWNER(HttpStatus.FORBIDDEN, "상품 소유자가 아닙니다."),
  FORBIDDEN_ONLY_SELLER(HttpStatus.FORBIDDEN, "해당 요청은 판매자만 수행할 수 있습니다.");

  private final HttpStatus status;
  private final String message;
}
