package com.barofarm.buyer.cart.application.dto;

import java.util.List;
import java.util.UUID;

public record CartValidationInfo(
    boolean isValid,
    UUID cartId,
    Long finalTotalPrice,
    List<ValidationError> errors
) {

    /** 재고 검증 성공 시 반환
     * @return {
     *   "isValid": true,
     *   "cartId": "UUID",
     *   "finalTotalPrice": 55000,
     *   "errors": []
     * }
     */
    public static CartValidationInfo success(UUID cartId, Long finalTotalPrice) {
        return new CartValidationInfo(true, cartId, finalTotalPrice, List.of());
    }

    /** 재고 검증 성공 시 반환
     * @return {
     *   "isValid": false,
     *   "errors": [
     *     { "productId": "1003", "message": "재고 부족" },
     *     { "productId": "2001", "message": "가격 변경됨" }
     *   ]
     * }
     */
    public static CartValidationInfo error(List<ValidationError> errors) {
        return new CartValidationInfo(false, null, null, errors);
    }

    // 에러 객체
    public record ValidationError(UUID productId, String message) {}
}
