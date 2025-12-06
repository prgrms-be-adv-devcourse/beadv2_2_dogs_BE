package com.barofarm.buyer.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  // 400 에러
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 제품입니다."),
  PRODUCT_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "해당 상품은 현재 판매 중이 아닙니다."),
  FORBIDDEN_NOT_PRODUCT_OWNER(HttpStatus.FORBIDDEN, "요청한 사용자는 해당 상품의 소유자가 아닙니다."),

  // 500 에러
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");

  private final HttpStatus status;
  private final String message;
}
