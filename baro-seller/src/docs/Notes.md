## 변경 로그

- 2025-12-09: Feign 예외 코드 추가 (Auth 호출 실패 시 CustomException 매핑)
  - `seller.exception.FeignErrorCode` 신설 (`AUTH_SERVICE_ERROR`, `AUTH_SERVICE_UNAVAILABLE`, `AUTH_SERVICE_TIMEOUT`).
  - `SellerService.applyForSeller`에서 Feign 예외를 잡아 `CustomException`으로 변환.
