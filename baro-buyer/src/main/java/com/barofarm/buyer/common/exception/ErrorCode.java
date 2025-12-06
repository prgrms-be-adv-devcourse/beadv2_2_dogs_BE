package com.barofarm.buyer.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  // 공통
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

  // Product
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
  FORBIDDEN_NOT_PRODUCT_OWNER(HttpStatus.FORBIDDEN, "상품 소유자가 아닙니다."),
  FORBIDDEN_ONLY_SELLER(HttpStatus.FORBIDDEN, "해당 요청은 판매자만 수행할 수 있습니다."),
  PRODUCT_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "해당 상품은 현재 판매 중이 아닙니다.");

  private final HttpStatus status;
  private final String message;
}
