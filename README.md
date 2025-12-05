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
- **Database**
  - Spring Data JPA
  - MySQL 8.0
- **Cache**
  - Redis 7.2
  - Spring Data Redis
- **Message Queue**
  - Apache Kafka 3.7.2
  - Spring for Apache Kafka
- **Code Quality**
  - Spotless 7.0.2 (Google Java Format 1.25.2)
  - Checkstyle 10.21.4

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

### 1️⃣ 인프라 서비스 실행 (선행 요구사항)

**Docker Compose로 한 번에 실행 (권장):**

```bash
# 모든 인프라 서비스 실행 (Redis + Kafka + Zookeeper)
docker-compose up -d

# 특정 서비스만 실행
docker-compose -f docker-compose-redis.yml up -d   # Redis만
docker-compose -f docker-compose-kafka.yml up -d   # Kafka만

# 중지
docker-compose down
```

**개별 실행:**

```bash
# Redis (6379)
docker run -d --name baro-redis -p 6379:6379 redis:7.2

# Kafka (9092)
docker run -d --name baro-zookeeper -p 2181:2181 -e ALLOW_ANONYMOUS_LOGIN=yes bitnami/zookeeper:3.9
docker run -d --name baro-kafka -p 9092:9092 -e KAFKA_ZOOKEEPER_CONNECT=host.docker.internal:2181 bitnami/kafka:3.6
```

**📚 상세 가이드:**
- [Redis 설정 가이드](docs/REDIS_SETUP.md) - 설치, 연동, 사용 예시
- [Kafka 설정 가이드](docs/KAFKA_SETUP.md) - 설치, 연동, 토픽 관리

### 2️⃣ Spring Boot 서비스 실행

#### Gradle로 실행

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

#### JAR로 실행

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

| 구분 | 모듈 | 포트 | 포함 도메인 |
|------|------|------|------------|
| **인프라** | redis | 6379 | Cache Server |
| | kafka | 9092 | Message Broker |
| **Spring Cloud** | eureka | 8761 | Service Registry |
| | config | 8888 | Config Server |
| | gateway | 8080 | API Gateway |
| **비즈니스** | baro-auth | 8081 | auth |
| | baro-buyer | 8082 | buyer, cart, product |
| | baro-seller | 8085 | seller, farm |
| | baro-order | 8087 | order, payment |
| | baro-support | 8089 | settlement, delivery, notification, experience, search, review |

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

> **💡 브랜치명은 영문으로, 커밋 메시지는 한글로 작성합니다.**  
| 브랜치 | 용도 | 예시 |
|--------|------|------|
| `main` | 최종 배포 버전 | - |
| `main-{모듈}` | 모듈별 안정 버전 | `main-buyer` |
| `dev-{모듈}` | 모듈별 개발 통합 | `dev-buyer` |
| `feature/issue-{이슈번호}-{기능설명-영문}` | 기능 개발 | `feature/issue-123-add-cart-item` |
| `fix/issue-{이슈번호}-{버그설명-영문}` | 버그 수정 | `fix/issue-456-product-search-error` |
| `hotfix/issue-{이슈번호}-{긴급수정-영문}` | 긴급 버그 수정 | `hotfix/issue-789-payment-failure` |

### 작업 흐름

```bash
# 1. GitHub에서 이슈 생성 (예: #123 장바구니 담기 기능)

# 2. dev 브랜치에서 feature 브랜치 생성
git checkout dev-buyer
git checkout -b feature/issue-123-add-cart-item

# 3. 작업 후 커밋 (커밋 메시지는 한글 사용)
git add .
git commit -m "[Feat] #123 - 장바구니 담기 기능 추가"

# 4. dev 브랜치로 머지
git checkout dev-buyer
git merge feature/issue-123-add-cart-item

# 5. 테스트 후 main 브랜치로 머지
git checkout main-buyer
git merge dev-buyer
```

### 커밋 메시지 규칙

```
[타입] #이슈번호 - 설명

예시:
[Feat] #123 - 회원가입 기능 추가
[Fix] #456 - 수량 변경 버그 수정
[Refactor] #789 - 상품 조회 로직 개선
[Docs] #321 - README 브랜치 전략 추가
```

| 타입 | 설명 |
|------|------|
| `Feat` | 새로운 기능 추가 |
| `Fix` | 버그 수정, 파일 등 삭제 |
| `Docs` | 문서 수정 |
| `Refactor` | 코드 리팩토링 |
| `Test` | 테스트 코드, 리팩토링 테스트 코드 추가 |
| `Chore` | 패키지 매니저 수정, 그 외 기타 수정 (ex: .gitignore) |

## 🚀 CI/CD

### GitHub Actions 자동 배포

이 프로젝트는 GitHub Actions를 통해 자동으로 빌드, 테스트, 배포됩니다.

#### 파이프라인

```
Push to main → CI (빌드/테스트) → Docker Image Build → 
Docker Hub Push → AWS EC2 Deploy → Health Check
```

#### 배포 프로세스

```bash
# 1. 코드 커밋 및 Push
git add .
git commit -m "[Feat] #123 - 새로운 기능 추가"
git push origin dev-{모듈}
- main-{모듈}에 PR 요청

# 2. GitHub Actions 자동 실행
- 코드 품질 검사 (Spotless, Checkstyle)
- 빌드 및 테스트
- Docker 이미지 빌드
- AWS EC2 배포

# 3. 배포 확인
# http://your-ec2-ip:8761 (Eureka Dashboard)
# http://your-ec2-ip:8080 (API Gateway)
```

#### 필요한 GitHub Secrets

| Secret | 설명 | 필요 여부 |
|--------|------|----------|
| `GITHUB_TOKEN` | GitHub Container Registry 인증 (자동 제공) | ✅ 자동 |
| `EC2_HOST` | EC2 Public IP | ✅ 필수 |
| `EC2_USERNAME` | EC2 SSH 사용자명 (예: ubuntu) | ✅ 필수 |
| `EC2_SSH_KEY` | EC2 SSH Private Key (.pem 파일 내용) | ✅ 필수 |

**참고:** `GITHUB_TOKEN`은 GitHub Actions가 자동으로 제공하므로 별도 설정 불필요!

### 버전 관리 및 롤백

#### 자동 생성되는 이미지 태그

```
ghcr.io/do-develop-space/baro-auth:
├── latest                         # 최신 버전
├── main-auth                      # 브랜치명
├── main-auth-abc123d              # 브랜치-커밋SHA
└── main-auth-20241205-143022      # 브랜치-타임스탬프
```

#### 롤백 방법

```bash
# EC2에서 실행

# 1. 사용 가능한 버전 확인
bash list-versions.sh auth

# 2. 이전 버전으로 롤백
bash rollback.sh auth main-auth-def456e

# 3. 확인
curl http://localhost:8081/actuator/health
```

#### 자동 정리

- ✅ 배포 성공 후 오래된 이미지 자동 삭제
- ✅ 최근 5개 버전만 GHCR에 유지
- ✅ EC2 로컬 이미지 수동 정리 가능 (`cleanup-images.sh`)

**📚 상세 가이드:**
- [CI/CD 설정 가이드](docs/CICD_GUIDE.md) - 전체 설정 및 트러블슈팅
- [버전 관리 가이드](docs/VERSION_MANAGEMENT.md) - 롤백 및 버전 관리

## 📝 라이선스

