package com.barofarm.buyer.product.exception;

import com.barofarm.buyer.common.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FarmErrorCode implements BaseErrorCode {
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
  FORBIDDEN_NOT_PRODUCT_OWNER(HttpStatus.FORBIDDEN, "상품 소유자가 아닙니다."),
  FORBIDDEN_ONLY_SELLER(HttpStatus.FORBIDDEN, "해당 요청은 판매자만 수행할 수 있습니다."),
  PRODUCT_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "해당 상품은 현재 판매 중이 아닙니다.");

  private final HttpStatus httpStatus;
  private final String message;
}
