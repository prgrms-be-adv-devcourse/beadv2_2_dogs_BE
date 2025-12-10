# Baro Farm - 마이크로서비스 백엔드

Spring Boot 4.0.0 + JDK 21 기반 멀티 모듈 프로젝트

## 📦 프로젝트 구조 (모듈러 모놀리스)

> 자세한 구조는 [BARO_FARM_STRUCTURE.md](docs/BARO_FARM_STRUCTURE.md) 참고

```
baro-farm/
├── baro-auth/                    # A. 인증 모듈
│   ├── src/main/java/com/barofarm/auth/
│   │   ├── AuthApplication.java
│   │   └── auth/                 # 인증/인가 도메인
│   └── build.gradle
│
├── baro-buyer/                   # B. 구매자 모듈
│   ├── src/main/java/com/barofarm/buyer/
│   │   ├── BuyerApplication.java
│   │   ├── buyer/                # 구매자 회원 관리
│   │   ├── cart/                 # 장바구니 관리
│   │   └── product/              # 상품 관리
│   └── build.gradle
│
├── baro-seller/                  # C. 판매자 모듈
│   ├── src/main/java/com/barofarm/seller/
│   │   ├── SellerApplication.java
│   │   ├── seller/               # 판매자 회원 관리
│   │   └── farm/                 # 농장 관리
│   └── build.gradle
│
├── baro-order/                   # D. 주문 모듈
│   ├── src/main/java/com/barofarm/order/
│   │   ├── OrderApplication.java
│   │   ├── order/                # 주문 관리
│   │   └── payment/              # 결제 관리
│   └── build.gradle
│
├── baro-support/                 # E. 지원 모듈
│   ├── src/main/java/com/barofarm/support/
│   │   ├── SupportApplication.java
│   │   ├── settlement/           # 정산 관리
│   │   ├── delivery/             # 배송 관리
│   │   ├── notification/         # 알림 관리
│   │   ├── experience/           # 체험 프로그램 관리
│   │   ├── search/               # 검색 관리
│   │   └── review/               # 리뷰 관리
│   └── build.gradle
│
└── baro-cloud/                   # F. 인프라 모듈
    ├── gateway/                  # API Gateway
    ├── config/                   # Config Server
    └── eureka/                   # Service Registry
```

## 🚀 기술 스택

- **Framework**: Spring Boot 4.0.0
- **Java**: JetBrains JDK 21
- **Build Tool**: Gradle 8.14
- **Spring Cloud**: 2025.0.0
  - Netflix Eureka (Service Discovery)
  - Spring Cloud Gateway
  - Spring Cloud Config
  - OpenFeign (서비스 간 통신)

## 🛠️ 개발 환경 설정

### 1. 프로젝트 클론 후 초기 설정

```bash
# 프로젝트 클론
git clone <repository-url>
cd beadv2_2_dogs_BE

# Git hooks 설치 (커밋 전 자동 검사)
./scripts/install-hooks.sh
```

### 2. 빌드

```bash
./gradlew build
```

## 🔍 코드 품질 관리

### 자동 검사 (커밋 시)

Git hooks가 설치되어 있으면 커밋할 때 자동으로 검사합니다.

### 수동 검사

```bash
# 전체 검사 (포맷 + 스타일)
./gradlew lint

# 포맷 검사만
./gradlew spotlessCheck

# 스타일 검사만 (lint)
./gradlew checkstyleMain
```

### 자동 수정

```bash
# 코드 포맷 자동 수정
./gradlew format
# 또는
./gradlew spotlessApply
```

## 🏃 서비스 실행 방법

### Gradle로 실행

```bash
# 1. Eureka Server (서비스 디스커버리)
./gradlew :baro-cloud:eureka:bootRun

# 2. Config Server (설정 서버) - 선택사항
./gradlew :baro-cloud:config:bootRun

# 3. Gateway Service (API Gateway)
./gradlew :baro-cloud:gateway:bootRun

# 4. 비즈니스 모듈 실행
./gradlew :baro-auth:bootRun      # 인증 모듈
./gradlew :baro-buyer:bootRun     # 구매자 모듈 (buyer + cart + product)
./gradlew :baro-seller:bootRun    # 판매자 모듈 (seller + farm)
./gradlew :baro-order:bootRun     # 주문 모듈 (order + payment)
./gradlew :baro-support:bootRun   # 지원 모듈 (6개 도메인)
```

### JAR로 실행

```bash
# 빌드
./gradlew build

# 실행
java -jar baro-cloud/eureka/build/libs/eureka-0.0.1-SNAPSHOT.jar
java -jar baro-cloud/config/build/libs/config-0.0.1-SNAPSHOT.jar
java -jar baro-cloud/gateway/build/libs/gateway-0.0.1-SNAPSHOT.jar
java -jar baro-auth/build/libs/baro-auth-0.0.1-SNAPSHOT.jar
java -jar baro-buyer/build/libs/baro-buyer-0.0.1-SNAPSHOT.jar
java -jar baro-seller/build/libs/baro-seller-0.0.1-SNAPSHOT.jar
java -jar baro-order/build/libs/baro-order-0.0.1-SNAPSHOT.jar
java -jar baro-support/build/libs/baro-support-0.0.1-SNAPSHOT.jar
```

## 🌐 서비스 포트 정보

| 모듈 | 포트 | 포함 도메인 |
|------|------|------------|
| eureka | 8761 | Service Registry |
| config | 8888 | Config Server |
| gateway | 8080 | API Gateway |
| baro-auth | 8081 | auth |
| baro-buyer | 8082 | buyer, cart, product |
| baro-seller | 8085 | seller, farm |
| baro-order | 8087 | order, payment |
| baro-support | 8089 | settlement, delivery, notification, experience, search, review |

## 🔗 주요 URL

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Config Server**: http://localhost:8888

## 📋 API 경로

모든 API는 Gateway를 통해 접근합니다:

| 서비스 | 경로 |
|--------|------|
| Auth | `/api/auth/**` |
| Buyer | `/api/buyers/**` |
| Cart | `/api/carts/**` |
| Product | `/api/products/**` |
| Seller | `/api/sellers/**` |
| Farm | `/api/farms/**` |
| Order | `/api/orders/**` |
| Payment | `/api/payments/**` |
| Settlement | `/api/settlements/**` |
| Delivery | `/api/deliveries/**` |
| Notification | `/api/notifications/**` |
| Experience | `/api/experiences/**` |
| Search | `/api/search/**` |
| Review | `/api/reviews/**` |

## 🔒 인증

Gateway의 `AuthenticationFilter`에서 JWT 토큰을 검증합니다.
인증이 필요한 API 호출 시 `Authorization: Bearer {token}` 헤더가 필요합니다.

## 🌿 브랜치 전략

### 브랜치 구조

```
main                          # 최종 배포 (Production)
 │
 ├── main-auth                # Auth 모듈 안정 버전
 ├── main-buyer               # Buyer 모듈 안정 버전
 ├── main-seller              # Seller 모듈 안정 버전
 ├── main-order               # Order 모듈 안정 버전
 ├── main-support             # Support 모듈 안정 버전
 └── main-cloud               # Cloud 모듈 안정 버전
      │
      ├── dev-auth            # Auth 모듈 개발
      ├── dev-buyer           # Buyer 모듈 개발
      ├── dev-seller          # Seller 모듈 개발
      ├── dev-order           # Order 모듈 개발
      ├── dev-support         # Support 모듈 개발
      └── dev-cloud           # Cloud 모듈 개발
           │
           └── feature/...    # 기능 개발 브랜치
```

### 브랜치 네이밍 규칙

| 브랜치 | 용도 | 예시 |
|--------|------|------|
| `main` | 최종 배포 버전 | - |
| `main-{모듈}` | 모듈별 안정 버전 | `main-buyer` |
| `dev-{모듈}` | 모듈별 개발 통합 | `dev-buyer` |
| `feature/{서비스}-{기능}` | 기능 개발 | `feature/cart-add-item` |
| `fix/{서비스}-{버그}` | 버그 수정 | `fix/product-search-error` |
<!-- | `hotfix/{긴급수정}` | 긴급 버그 수정 | `hotfix/payment-failure` | -->

### 작업 흐름

```bash
# 1. dev 브랜치에서 feature 브랜치 생성
git checkout dev-buyer
git checkout -b feature/cart-add-item

# 2. 작업 후 커밋
git add .
git commit -m "[feat] 장바구니 담기 기능 추가"

# 3. dev 브랜치로 머지
git checkout dev-buyer
git merge feature/cart-add-item

# 4. 테스트 후 main 브랜치로 머지
git checkout main-buyer
git merge dev-buyer
```

### 커밋 메시지 규칙

```
[타입] 설명

예시:
[Feat] 회원가입 기능 추가
[Fix] 수량 변경 버그 수정
[Refactor] 상품 조회 로직 개선
[Docs] README 브랜치 전략 추가
```

| 타입 | 설명 |
|------|------|
| `Feat` | 새로운 기능 추가 |
| `Fix` | 버그 수정, 파일 등 삭제 |
| `Docs` | 문서 수정 |
| `Refactor` | 코드 리팩토링 |
| `Test` | 테스트 코드, 리팩토링 테스트 코드 추가 |
| `Chore` | 패키지 매니저 수정, 그 외 기타 수정 (ex: .gitignore) |

## 📝 라이선스

