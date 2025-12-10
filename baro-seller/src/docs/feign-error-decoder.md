# Feign ErrorDecoder 적용 가이드

## ErrorDecoder란?
- OpenFeign이 2xx가 아닌 응답을 받았을 때 예외를 어떻게 변환할지 커스터마이징하는 인터페이스다.
- 기본값은 상태 코드와 본문을 포함한 `FeignException`을 던진다.
- 서비스를 기준으로 공통 예외(예: `CustomException`)를 던지게 하면 서비스 코드에서 중복 `try-catch` 없이 동일한 방식으로 처리할 수 있다.

## 이번 적용 내용
- 설정 클래스: `com.barofarm.seller.seller.config.FeignErrorDecoder`
  - 504 → `FeignErrorCode.AUTH_SERVICE_TIMEOUT`
  - 502/503 → `FeignErrorCode.AUTH_SERVICE_UNAVAILABLE`
  - 기타 5xx → `FeignErrorCode.AUTH_SERVICE_ERROR`
  - 나머지 상태 코드는 기본 디코더 동작(기존 `FeignException`)을 그대로 사용.
- Bean 등록: `FeignAuthConfig`에 `ErrorDecoder` Bean 추가.
- 서비스 코드 정리: `SellerService.applyForSeller`에서 Feign 예외를 직접 캐치하지 않고, 디코더가 던진 `CustomException`을 글로벌 핸들러가 처리하도록 단순화.

## 사용/확장 방법
1. 새로 추가되는 Feign 클라이언트가 Auth 서비스가 아니더라도 같은 방식으로 처리하고 싶다면 해당 클라이언트의 `configuration`에 `FeignAuthConfig`를 재사용하거나 전역 `ErrorDecoder` Bean을 등록한다.
2. 다른 서비스에 대한 에러 코드를 추가할 때는 `FeignErrorCode`에 항목을 추가하고, `FeignErrorDecoder`의 매핑 조건을 함께 보강한다.
3. 상태 코드 외에 본문까지 해석해야 할 경우, `Response`의 body를 읽어 메시지를 구성한 뒤 `CustomException`을 생성하도록 확장한다(본문 스트림은 한 번만 읽을 수 있으므로 주의).

## 기대 효과
- Feign 호출 실패 처리 로직을 한 곳에 모아 서비스 레이어가 단순해진다.
- 공통 `CustomException`으로 변환되므로 기존 `GlobalExceptionHandler`만으로 일관된 HTTP 응답을 내려줄 수 있다.
