# Baro Farm - 마이크로서비스 백엔드

Spring Boot 3.5.8 + JDK 21 기반 멀티 모듈 프로젝트

## 📦 프로젝트 구조 (MSA 구조)

> 자세한 구조는 [BARO_FARM_STRUCTURE.md](docs/BARO_FARM_STRUCTURE.md) 참고

```
baro-farm/
├── baro-auth/                    # A. 인증 모듈
│   ├── src/main/java/com/barofarm/auth/
│   │   ├── AuthApplication.java
│   │   └── auth/                 # 인증/인가 도메인, 구매자 및 회원관리 통합
│   └── build.gradle
│
├── baro-buyer/                   # B. 구매자 모듈
│   ├── src/main/java/com/barofarm/buyer/
│   │   ├── BuyerApplication.java
│   │   ├── cart/                 # 장바구니 관리
│   │   ├── product/              # 상품 관리
│   │   └── inventory/            # 재고 관리
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
│   │   └── order/                # 주문 관리
│   └── build.gradle
│
├── baro-payment/                 # E. 주문 모듈
│   ├── src/main/java/com/barofarm/payment/
│   │   ├── PaymentApplication.java
│   │   └── payment/              # 결제 관리
│   └── build.gradle
│
├── baro-support/                 # F. 지원 모듈
│   ├── src/main/java/com/barofarm/support/
│   │   ├── SupportApplication.java
│   │   ├── delivery/             # 배송 관리
│   │   ├── notification/         # 알림 관리
│   │   ├── experience/           # 체험 프로그램 관리
│   │   ├── search/               # 검색 관리
│   │   ├── review/               # 리뷰 관리
│   │   └── deposit/              # 예치금 관리
│   └── build.gradle
│
├── baro-settlement/              # G. 정산 모듈
│   ├── src/main/java/com/barofarm/settlement/
│   │   ├── SettlementApplication.java
│   │   └── settlement/           # 정산 관리 (DaemonSet 배포)
│   └── build.gradle
│
├── baro-ai/                      # H. AI 모듈
│   ├── src/main/java/com/barofarm/ai/
│   │   ├── AiApplication.java
│   │   ├── eventLog/
│   │   ├── recommend/            # 추천 서비스
│   │   ├── review/               # 리뷰 서비스
│   │   └── season/               # 제철 서비스
│   └── build.gradle
│
└── baro-cloud/                   # H. 인프라 모듈
    ├── gateway/                  # API Gateway
    ├── config/                   # Config Server
    └── eureka/                   # Service Registry
```

## 🚀 기술 스택

- **Framework**: Spring Boot 3.5.8
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
  - Confluent Kafka 7.6.0 (Apache Kafka 호환)
  - KRaft 모드 (Zookeeper 불필요)
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
# 모든 인프라 서비스 실행 (Redis + Kafka KRaft 모드)
docker-compose -f docker-compose.data.yml up -d

# 특정 서비스만 실행
docker-compose -f docker-compose.data.yml up -d redis   # Redis만
docker-compose -f docker-compose.data.yml up -d kafka   # Kafka만

# 중지
docker-compose -f docker-compose.data.yml down
```

**개별 실행:**

```bash
# Redis (6379)
docker run -d --name baro-redis -p 6379:6379 redis:7.2

# Kafka (9092) - KRaft 모드 (Zookeeper 불필요)
docker run -d --name baro-kafka \
  -p 9092:9092 \
  -p 9093:9093 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 \
  -e KAFKA_AUTO_CREATE_TOPICS_ENABLE=true \
  confluentinc/cp-kafka:7.6.0
```

**📚 상세 가이드:**
- [Redis 설정 가이드](docs/REDIS_SETUP.md) - 설치, 연동, 사용 예시
- [Kafka 설정 가이드](docs/KAFKA_SETUP.md) - 설치, 연동, 토픽 관리

### 2️⃣ Spring Boot 서비스 실행

#### Gradle로 실행

```bash
# 1. Eureka Server (서비스 디스커버리)
./gradlew :baro-cloud:eureka:bootRun

# 2. Config Server (설정 서버)
./gradlew :baro-cloud:config:bootRun

# 3. Gateway Service (API Gateway)
./gradlew :baro-cloud:gateway:bootRun

# 4. 비즈니스 모듈 실행
./gradlew :baro-auth:bootRun      # 인증 모듈
./gradlew :baro-buyer:bootRun     # 구매자 모듈 (cart, product, inventory)
./gradlew :baro-seller:bootRun    # 판매자 모듈 (seller, farm)
./gradlew :baro-order:bootRun     # 주문 모듈 (order)
./gradlew :baro-payment:bootRun   # 결제 모듈 (payment)
./gradlew :baro-support:bootRun   # 지원 모듈 (delivery, notification, experience, review, deposit)
./gradlew :baro-settlement:bootRun # 정산 모듈 (settlement)
./gradlew :baro-ai:bootRun        # AI 모듈 (search, recommend, season)
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
java -jar baro-payment/build/libs/baro-payment-0.0.1-SNAPSHOT.jar
java -jar baro-support/build/libs/baro-support-0.0.1-SNAPSHOT.jar
java -jar baro-settlement/build/libs/baro-settlement-0.0.1-SNAPSHOT.jar
java -jar baro-ai/build/libs/baro-ai-0.0.1-SNAPSHOT.jar
```

## 🌐 서비스 포트 정보

| 구분 | 모듈 | 포트 | 포함 도메인 |
|------|------|------|------------|
| **인프라** | redis | 6379 | Cache Server |
| | kafka | 29092 | Message Broker (KRaft 모드) |
| | mysql | 3306 | Database |
| | elasticsearch | 9200 | Search Engine |
| **Spring Cloud** | eureka | 8761 | Service Registry |
| | config | 8888 | Config Server |
| | gateway | 8080 | API Gateway |
| **비즈니스** | baro-auth | 8081 | auth |
| | baro-buyer | 8082 | buyer, cart, product |
| | baro-seller | 8085 | seller, farm |
| | baro-order | 8087 | order |
| | baro-payment | 8088 | payment |
| | baro-support | 8089 | delivery, notification, experience, review, deposit |
| | baro-settlement | 8090 | settlement (DaemonSet 배포) |
| | baro-ai | 8092 | search, recommend, review, season |

## 💾 리소스 제한 사항

> **t3.medium (4GB RAM) 2대 환경 최적화 설정**  
> 모든 서비스의 메모리 사용량을 제한하여 안정적인 운영을 보장합니다.
> - **Master Node**: MySQL, Redis, Elasticsearch, Eureka, Config, Gateway 배포
> - **Worker Node**: Kafka, 비즈니스 서비스 모듈 배포

### Spring Cloud 인프라 모듈

| 서비스 | 메모리 제한 (JVM) | Healthcheck | 비고 |
|--------|------------------|-------------|------|
| **eureka** | `-Xms128m -Xmx256m` | 60s 간격 | Service Registry |
| **config** | `-Xms256m -Xmx384m` | 60s 간격 | Config Server |
| **gateway** | `-Xms256m -Xmx384m` | 60s 간격 | API Gateway |

**설정 파일:** `docker-compose.cloud.yml`

### 비즈니스 서비스 모듈

| 서비스 | 메모리 제한 (JVM) | 의존성 | 비고 |
|--------|------------------|--------|------|
| **baro-auth** | `-Xms64m -Xmx128m` | MySQL | 인증/인가 서비스, 구매자 |
| **baro-buyer** | `-Xms64m -Xmx128m` | MySQL, Kafka | 장바구니, 상품 관리 |
| **baro-seller** | `-Xms64m -Xmx128m` | MySQL, Kafka | 판매자, 농장 관리 |
| **baro-order** | `-Xms64m -Xmx128m` | MySQL, Kafka | 주문 |
| **baro-payment** | `-Xms64m -Xmx128m` | MySQL, Kafka | 결제, 예치금 관리 |
| **baro-support** | `-Xms64m -Xmx128m` | MySQL, Kafka | 배송, 알림, 체험, 리뷰 관리 |
| **baro-settlement** | `-Xms64m -Xmx128m` | MySQL | 정산 관리 (DaemonSet 배포) |
| **baro-ai** | `-Xms64m -Xmx128m` | Kafka, Elasticsearch | 검색, 추천, 챗봇, 제철 AI |

**설정 파일:**
- `docker-compose.auth.yml`
- `docker-compose.buyer.yml`
- `docker-compose.seller.yml`
- `docker-compose.order.yml`
- `docker-compose.payment.yml`
- `docker-compose.support.yml`
- `docker-compose.settlement.yml`
- `docker-compose.ai.yml`

### 데이터 인프라 모듈

> **배포 노드 정보:**
> - **MySQL, Redis, Elasticsearch**: Master Node에 배포
> - **Kafka**: Worker Node에 배포

#### MySQL

| 설정 | 값 | 비고 |
|------|-----|------|
| **배포 노드** | Master Node | - |
| **InnoDB 버퍼 풀** | 256M | `innodb-buffer-pool-size` |
| **최대 연결 수** | 100 | `max-connections` |
| **예상 최대 메모리** | ~400MB | InnoDB 버퍼 풀 + 프로세스 메모리 |

**설정 위치:** `docker-compose.data.yml`

```yaml
command: --innodb-buffer-pool-size=256M --max-connections=100 --lower-case-table-names=1
```

**참고:** MySQL의 실제 메모리 사용량은 InnoDB 버퍼 풀(256M)과 프로세스 오버헤드(약 100-150M)를 합쳐 약 400MB 정도입니다.

#### Redis

| 설정 | 값 | 비고 |
|------|-----|------|
| **배포 노드** | Master Node | - |
| **최대 메모리** | 256MB | `maxmemory` |
| **메모리 정책** | allkeys-lru | LRU 기반 키 제거 |
| **AOF (Append Only File)** | 활성화 | 영속성 보장 |

**설정 위치:** `docker-compose.data.yml`

```yaml
command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD:-redis123} --maxmemory 256mb --maxmemory-policy allkeys-lru
```

#### Kafka

| 설정 | 값 | 비고 |
|------|-----|------|
| **배포 노드** | Worker Node | - |
| **JVM 힙 메모리** | `-Xms192m -Xmx256m` | Kafka 프로세스 메모리 |
| **Healthcheck 간격** | 60s | CPU 사용량 최적화 |
| **모드** | KRaft | Zookeeper 불필요 |

**설정 위치:** `docker-compose.kafka.yml`

```yaml
environment:
  KAFKA_HEAP_OPTS: -Xms192m -Xmx256m
init: true  # 좀비 프로세스 방지
```

#### Elasticsearch

| 설정 | 값 | 비고 |
|------|-----|------|
| **배포 노드** | Master Node | - |
| **JVM 힙 메모리** | `-Xms192m -Xmx384m` | Elasticsearch 프로세스 메모리 |
| **Healthcheck 간격** | 30s | 상태 확인 주기 |
| **Healthcheck 방식** | CMD (직접 실행) | 좀비 프로세스 방지 |
| **엔드포인트** | `/_cluster/health` | 클러스터 상태 확인 |

**설정 위치:** `docker-compose.elasticsearch.yml`

```yaml
environment:
  ES_JAVA_OPTS: -Xms192m -Xmx384m
init: true  # 좀비 프로세스 방지
healthcheck:
  test: ["CMD", "curl", "-sSf", "http://localhost:9200/_cluster/health"]
  interval: 30s
```

### 전체 메모리 사용량 요약

| 카테고리 | 서비스 | 메모리 (최대) |
|----------|--------|--------------|
| **Spring Cloud** | eureka | 300MB |
| | config | 300MB |
| | gateway | 300MB |
| | **소계** | **900MB** |
| **비즈니스 서비스** | baro-auth | 350MB |
| | baro-buyer | 400MB |
| | baro-seller | 350MB |
| | baro-order | 400MB |
| | baro-payment | 350MB |
| | baro-support | 400MB |
| | baro-settlement | 350MB |
| | baro-ai | 350MB |
| | **소계** | **1.95GB** |
| **데이터 인프라** | mysql | 400MB |
| | redis | 256MB |
| | kafka | 256MB |
| | elasticsearch | 600MB |
| | **소계** | **1.5GB** |
| **총합** | | **~4.35GB** |

> **참고:**
> - **Master Node (t3.medium 4GB)**: OS 및 Docker 데몬 ~1GB, 시스템 버퍼 ~0.5GB, 실제 사용 가능 ~2.5GB
> - **Worker Node (t3.medium 4GB)**: OS 및 Docker 데몬 ~1GB, 시스템 버퍼 ~0.5GB, 실제 사용 가능 ~2.5GB
> - Master Node에는 MySQL, Redis, Elasticsearch, Eureka, Config, Gateway를, Worker Node에는 Kafka와 비즈니스 서비스 모듈을 배포하여 메모리 사용량을 최적화

### 리소스 모니터링

```bash
# 실시간 리소스 사용량 확인
docker stats

# 특정 컨테이너 메모리 사용량 확인
docker stats baro-auth baro-buyer baro-seller

# 전체 메모리 사용량 확인
free -h
```

### 리소스 조정 가이드

메모리 부족 시 다음 순서로 조정을 고려하세요:

1. **Healthcheck 간격 증가** (CPU 사용량 감소)
   ```yaml
   healthcheck:
     interval: 60s  # 30s → 60s
   ```

2. **JVM GC 튜닝** (메모리 효율 향상)
   ```yaml
   JAVA_OPTS=-Xms64m -Xmx128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
   ```

3. **비활성 서비스 중지** (일시적)
   ```bash
   docker-compose -f docker-compose.order.yml stop
   ```

4. **인스턴스 타입 업그레이드** (장기적)
   - t3.medium (4GB) → t3.large (8GB) 또는 t3.xlarge (16GB)

## 🔗 주요 URL

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Config Server**: http://localhost:8888

## 📋 API 경로

모든 API는 Gateway를 통해 접근합니다: (Port: 8080)

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
| AI | `/api/ai/**` |

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
 ├── main-payment               # Payment 모듈 안정 버전
 ├── main-support             # Support 모듈 안정 버전
 ├── main-settlement          # Settlement 모듈 안정 버전
 ├── main-ai                  # AI 모듈 안정 버전
 └── main-cloud               # Cloud 모듈 안정 버전
      │
      ├── dev-auth            # Auth 모듈 개발
      ├── dev-buyer           # Buyer 모듈 개발
      ├── dev-seller          # Seller 모듈 개발
      ├── dev-order           # Order 모듈 개발
      ├── dev-payment           # Payment 모듈 개발
      ├── dev-support         # Support 모듈 개발
      ├── dev-settlement      # Settlement 모듈 개발
      ├── dev-ai              # AI 모듈 개발
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

### 인프라 구성

이 프로젝트는 **k3s 클러스터** 환경에서 운영됩니다.

#### 클러스터 구성

- **k3s Master Node (t3.medium 4GB)**
  - GitHub Actions Runner 설치
  - MySQL, Redis, Elasticsearch (Docker Compose)
  - Eureka, Config, Gateway (k3s Pod)
  - Kubernetes API Server, etcd 등 k3s 제어 플레인

- **k3s Worker Node (t3.medium 4GB)**
  - GitHub Actions Runner 설치
  - Kafka (Docker Compose)
  - 비즈니스 서비스 모듈 (k3s Pod)
    - baro-auth, baro-buyer, baro-seller, baro-order, baro-payment, baro-support, baro-settlement (DaemonSet), baro-ai

#### 배포 방식

- **데이터 인프라**: Docker Compose로 직접 배포
- **애플리케이션**: k3s를 통해 Kubernetes Pod로 배포
- **CI/CD**: 각 서버에 설치된 GitHub Actions Runner가 자동 배포 수행

### Kustomize를 활용한 Kubernetes 배포

이 프로젝트는 **Kustomize**를 사용하여 Kubernetes 매니페스트를 선언적으로 관리합니다.

#### Kustomize란?

Kustomize는 Kubernetes 네이티브 구성 관리 도구로, YAML 파일을 템플릿화하지 않고도 재사용 가능한 구성 패키지를 만들 수 있습니다.

#### 주요 특징

- **선언적 관리**: `kustomization.yaml` 파일을 통해 리소스를 선언적으로 관리
- **이미지 태그 관리**: `images` 섹션을 통해 이미지 태그를 쉽게 변경 가능
- **공통 라벨**: `labels`를 통한 일관된 라벨 관리
- **네임스페이스 관리**: 각 kustomization 파일에서 네임스페이스 자동 적용
- **환경별 구성**: base와 overlay를 통한 환경별 설정 분리

#### 디렉토리 구조

```
k8s/
├── base/                    # 기본 리소스
│   ├── namespace.yaml       # Namespace 정의
│   ├── secret.yaml.template # Secret 템플릿
│   └── kustomization.yaml   # Base kustomization 설정
│
├── cloud/                   # Spring Cloud 인프라 모듈
│   ├── eureka/
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   └── kustomization.yaml
│   ├── config/
│   └── gateway/
│
├── apps/                    # 비즈니스 애플리케이션 모듈
│   ├── baro-auth/
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   └── kustomization.yaml
│   ├── baro-buyer/
│   ├── baro-seller/
│   ├── baro-order/
│   ├── baro-payment/
│   ├── baro-support/
│   ├── baro-settlement/
│   └── baro-ai/
│
└── redis/                   # Redis 캐시: Docker Container로 별도 관리 
    ├── deployment.yaml
    ├── service.yaml
    └── kustomization.yaml
```

#### kustomization.yaml 예시

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: baro-prod

resources:
  - deployment.yaml
  - service.yaml

labels:
  - includeSelectors: false  # selector는 immutable이므로 false
    includeTemplates: true
    pairs:
      app: baro-auth
      component: app

images:
  - name: ghcr.io/do-develop-space/baro-auth
    newTag: latest  # 이미지 태그 변경
```

#### 배포 방법

```bash
# 1. Namespace 생성
kubectl apply -k k8s/base/

# 2. Cloud 모듈 배포 (순서 중요: Eureka → Config → Gateway)
kubectl apply -k k8s/cloud/eureka/
kubectl wait --for=condition=ready pod -l app=eureka -n baro-prod --timeout=300s

kubectl apply -k k8s/cloud/config/
kubectl wait --for=condition=ready pod -l app=config -n baro-prod --timeout=300s

kubectl apply -k k8s/cloud/gateway/

# 3. 비즈니스 서비스 모듈 배포
kubectl apply -k k8s/apps/baro-auth/
kubectl apply -k k8s/apps/baro-buyer/
kubectl apply -k k8s/apps/baro-seller/
kubectl apply -k k8s/apps/baro-order/
kubectl apply -k k8s/apps/baro-payment/
kubectl apply -k k8s/apps/baro-support/
kubectl apply -k k8s/apps/baro-settlement/  # DaemonSet 배포
kubectl apply -k k8s/apps/baro-ai/
```

#### 이미지 태그 변경

각 서비스의 `kustomization.yaml` 파일에서 이미지 태그를 변경할 수 있습니다:

```yaml
images:
  - name: ghcr.io/do-develop-space/baro-auth
    newTag: main-auth-abc123d  # 원하는 태그로 변경
```

또는 배포 스크립트(`scripts/deploy-k8s.sh`)를 사용하면 자동으로 이미지 태그가 업데이트됩니다.

#### Kustomize 명령어 활용

```bash
# 빌드된 매니페스트 미리보기 (실제 적용 전 확인)
kubectl kustomize k8s/apps/baro-auth/

# 빌드 + 적용 (권장)
kubectl apply -k k8s/apps/baro-auth/

# 특정 리소스만 확인
kubectl kustomize k8s/apps/baro-auth/ | grep -A 10 "kind: Deployment"
```

#### 장점

1. **템플릿 불필요**: Helm처럼 템플릿 엔진이 필요 없음
2. **네이티브 지원**: kubectl에 내장되어 있어 별도 설치 불필요
3. **버전 관리 용이**: Git으로 kustomization.yaml 파일을 관리
4. **환경별 구성**: base와 overlay를 통한 dev/staging/prod 분리 가능
5. **이미지 태그 관리**: CI/CD 파이프라인에서 쉽게 이미지 태그 변경

**📚 상세 가이드:**
### GitHub Actions 자동 배포

이 프로젝트는 각 서버에 설치된 **GitHub Actions Runner**를 통해 자동으로 빌드, 테스트, 배포됩니다.

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

# 2. GitHub Actions Runner 자동 실행 (각 서버에서 실행)
- 코드 품질 검사 (Spotless, Checkstyle)
- 빌드 및 테스트
- Docker 이미지 빌드
- GHCR (GitHub Container Registry)에 이미지 푸시
- k3s 클러스터에 배포 (kubectl apply)

# 3. 배포 확인
# http://your-ec2-ip:8761 (Eureka Dashboard)
# http://your-ec2-ip:8080 (API Gateway)
```

#### GitHub Actions Runner 설정

각 서버(Master Node, Worker Node)에 GitHub Actions Runner가 설치되어 있어, GitHub에서 워크플로우가 트리거되면 해당 Runner가 자동으로 작업을 수행합니다.

**Runner 설치 위치:**
- Master Node: `/home/ubuntu/actions-runner`
- Worker Node: `/home/ubuntu/actions-runner`

**Runner 특징:**
- Self-hosted Runner로 GitHub에서 직접 관리
- 각 서버에서 직접 실행되어 네트워크 지연 최소화
- k3s 클러스터에 직접 접근 가능 (`kubectl` 명령어 사용)

#### 필요한 GitHub Secrets

> GitHub Actions 워크플로우에서 사용하는 **민감 정보** 목록입니다.  
> 모든 Secret은 GitHub Repository → **Settings → Secrets and variables → Actions → Repository secrets** 에서 설정합니다.

| Secret | 설명 | 필요 여부 |
|--------|------|----------|
| `GITHUB_TOKEN` | GitHub Container Registry 인증 (GitHub에서 자동 제공) | ✅ 자동 |
| `GHCR_PAT` | GHCR 이미지 Push/Pull용 Personal Access Token (필요 시) | ⚠️ 선택 |
| `AWS_ACCESS_KEY` | AWS Access Key (S3, 기타 AWS 리소스 접근) | ✅ 필수 |
| `AWS_SECRET_KEY` | AWS Secret Key (S3, 기타 AWS 리소스 접근) | ✅ 필수 |
| `DATA_EC2_IP` | Data EC2 Private IP (MySQL/Redis/Kafka/ES 접속용) | ✅ 필수 |
| `EC2_HOST` | EC2 Public IP (SSH 접속, 수동 디버깅용) | ⚠️ 선택 |
| `EC2_USERNAME` | EC2 SSH 사용자명 (예: `ubuntu`) | ⚠️ 선택 |
| `EC2_SSH_KEY` | EC2 SSH Private Key (.pem 파일 내용) | ⚠️ 선택 |
| `TOSS_SECRET_KEY` | Toss Payments Secret Key (결제 모듈에서 사용) | ✅ 필수 |

**참고:** 
- `GITHUB_TOKEN`은 GitHub Actions가 자동으로 제공하므로 별도 설정이 필요 없습니다.
- `GHCR_PAT`은 조직/권한 구조에 따라 필요할 수 있으며, 필요 없으면 설정하지 않아도 됩니다.
- Self-hosted Runner를 사용하므로 SSH 관련 Secret(EC2_HOST, EC2_USERNAME, EC2_SSH_KEY)은 **디버깅/수동 작업용 선택사항**입니다.

#### GitHub Variables (환경 변수)

> GitHub Actions에서 사용하는 **비밀이 아닌 설정 값**입니다.  
> GitHub Repository → **Settings → Secrets and variables → Actions → Repository variables** 에서 설정합니다.

| Variable | 예시 값 | 설명 |
|----------|---------|------|
| `DEPLOY_KAFKA` | `true` / `false` | k3s 환경에서 Kafka를 배포할지 여부 (Docker Compose 기반 Kafka 사용 제어) |
| `DEPLOY_ELASTICSEARCH` | `true` / `false` | k3s 환경에서 Elasticsearch를 배포할지 여부 (Docker Compose 기반 ES 사용 제어) |


### 버전 관리 및 롤백

#### 자동 생성되는 이미지 태그

```
ghcr.io/do-develop-space/baro-auth:
├── latest                         # 최신 버전
├── main-auth                      # 브랜치명
├── main-auth-abc123d              # 브랜치-커밋SHA
└── main-auth-20241205-143022      # 브랜치-타임스탬프
```

#### 배포 이미지 버전

- **공통 이미지 버전 환경 변수**: `IMAGE_VERSION=0.1.0`
- **이미지 태그 규칙**:
  - 애플리케이션: `ghcr.io/do-develop-space/{모듈}:IMAGE_VERSION` (예: `ghcr.io/do-develop-space/baro-auth:0.1.0`)
  - 필요 시 브랜치/커밋 SHA와 조합: `IMAGE_VERSION-main-auth-abc123d`
  - k8s 배포 시 `kustomization.yaml` 또는 배포 스크립트(`scripts/deploy-k8s.sh`)에서 `IMAGE_VERSION`을 기준으로 태그를 치환

#### 롤백 방법

```bash
# k3s Worker Node에서 실행 (비즈니스 서비스의 경우)

# 1. 사용 가능한 이미지 태그 확인
kubectl get deployment baro-auth -n baro-prod -o jsonpath='{.spec.template.spec.containers[0].image}'

# 2. kustomization.yaml에서 이미지 태그 변경
cd k8s/apps/baro-auth
# images 섹션의 newTag를 이전 버전으로 변경

# 3. k3s에 재배포
kubectl apply -k .

# 4. 배포 상태 확인
kubectl get pods -n baro-prod -l app=baro-auth
kubectl logs -n baro-prod -l app=baro-auth --tail=50

# 5. Health Check
curl http://localhost:8081/actuator/health
```

#### 자동 정리

- ✅ 배포 성공 후 오래된 이미지 자동 삭제
- ✅ 최근 5개 버전만 GHCR에 유지
- ✅ EC2 로컬 이미지 수동 정리 가능 (`cleanup-images.sh`)

**📚 상세 가이드:**
- [CI/CD 설정 가이드](docs/CICD_GUIDE.md) - 전체 설정 및 트러블슈팅

## 📝 라이선스

