## 변경 로그

- 2025-12-09: Feign 예외 코드 추가 (Auth 호출 실패 시 CustomException 매핑)
  - `seller.exception.FeignErrorCode` 신설 (`AUTH_SERVICE_ERROR`, `AUTH_SERVICE_UNAVAILABLE`, `AUTH_SERVICE_TIMEOUT`).
  - `SellerService.applyForSeller`에서 Feign 예외를 잡아 `CustomException`으로 변환.

## 변경 로그 (Seller Service)

- 판매자 신청 시 DB 커밋 이후에만 auth-service 권한 부여 Feign 호출 실행(afterCommit 적용).
- userId 중복 시 SELLER_ALREADY_EXISTS 에러코드로 중복 등록 차단.
- Feign 호출 실패 시 트랜잭션은 유지되며 이후 단계에서 재시도/보상 로직 필요.
