# 바로팜(Baro-Farm) 세미 프로젝트 계획서

> **프로젝트명**: 바로팜 (Baro-Farm)  
> **프로젝트 기간**: 2025년 12월 1일 ~ 12월 17일 (16일, 약 2.5주)  
> **프로젝트 목표**: 핵심 MVP 완성 및 시연 가능한 수준 구현  
> **팀 구성**: 5명 (PO, 아키텍트, 백엔드1, 백엔드2, AI)

---

## 📋 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [프로젝트 목표 및 범위](#2-프로젝트-목표-및-범위)
3. [기술 스택 및 아키텍처](#3-기술-스택-및-아키텍처)
4. [프로젝트 일정](#4-프로젝트-일정)
5. [팀 역할 및 책임](#5-팀-역할-및-책임)
6. [CI/CD 파이프라인](#6-cicd-파이프라인)
7. [배포 전략](#7-배포-전략)
8. [리스크 관리](#8-리스크-관리)
9. [성공 기준](#9-성공-기준)
10. [문서화](#10-문서화)
11. [다음 단계](#11-다음-단계-최종)

---

## 1. 프로젝트 개요

### 1.1 프로젝트 비전

**바로팜(Baro-Farm)**은 신선한 농산물을 생산자로부터 소비자에게 직접 연결하는 **Farm-to-Table 이커머스 플랫폼**입니다. 도시 소비자와 농촌 생산자를 연결하여 신뢰 기반의 직거래를 실현하고, 농장 체험 서비스를 통해 지속가능한 농업 생태계를 구축합니다.

### 1.2 핵심 가치

- **신선함**: 생산자로부터 직접 배송되는 신선한 농산물
- **신뢰**: 투명한 생산 정보와 리뷰 시스템
- **지속가능성**: 로컬푸드 운동을 통한 환경 보호
- **연결**: 도시와 농촌을 잇는 디지털 플랫폼

### 1.3 주요 기능

#### 핵심 기능 (MVP)
- ✅ 회원 등록 및 관리
- ✅ 사용자 인증/인가 (JWT 기반)
- ✅ 농장 등록 및 관리
- ✅ 상품 등록 및 관리
- ✅ 리뷰 및 평점 시스템
- ✅ 주문 및 결제 처리
- ✅ 정산 관리
- ✅ 배송 관리
- ✅ 상품 검색 (Elasticsearch)
- ✅ 장바구니 기능
- ✅ 농장 체험 예약
- ✅ 알림 시스템
- ✅ 검색

#### 향후 확장 기능
- 구독 시스템 (밀키트 등 정기 배송)
- 쿠폰 관리
- 통계
- AI 기반 추천 시스템

---

## 2. 프로젝트 목표 및 범위

### 2.1 프로젝트 목표

#### 단기 목표 (MVP)
- [x] 마이크로서비스 아키텍처 기반 시스템 구축
- [x] 핵심 비즈니스 로직 구현 (주문 → 결제 → 배송, 정산)
- [x] 이벤트 기반 아키텍처 (Kafka) 구현 (알림, 주문, 예약, 결제)
- [x] CI/CD 파이프라인 구축 (Github Actions)
- [x] Kubernetes 기반 배포 환경 구축 (세미 이후로 도입 예정)
- [x] 시연 가능한 수준의 완성도 달성

#### 장기 목표 (Phase 2+)
- 확장 기능 추가 (AI 추천, 쿠폰, 구독 서비스)
- 모니터링 및 로깅 시스템 고도화
- 성능 최적화 및 확장성 개선

### 2.2 프로젝트 범위

#### 포함 범위 (In-Scope)
- **13개 핵심 마이크로서비스**
  - member-service (회원 관리)
  - auth-service (인증/인가)
  - farm-service (농장 관리)
  - product-service (상품 관리)
  - order-service (주문 관리)
  - payment-service (결제 관리)
  - delivery-service (배송 관리)
  - search-service (검색)
  - cart-service (장바구니)
  - settlement-service(정산)
  - notification-service (알림)
  - experience-service (체험 예약)
  - review-service (리뷰/평점)
  - api-gateway (API 게이트웨이), 세미 이후로 도입 예정

- **인프라 및 DevOps**
  - Docker 컨테이너화
  - GitHub Actions CI/CD
  - GHCR (GitHub Container Registry)
  - Kubernetes 오케스트레이션, 세미 이후로 도입 예정

- **데이터베이스**
  - PostgreSQL (1개의 DB 내에서 Schema로 분리)
  - Redis (캐싱)
  - Elasticsearch (검색)

- **프론트엔드**
  - Vercel AI를 활용한 시연 화면 개발

#### 제외 범위 (Out-of-Scope)
- 실제 택배사 API 연동 (Mock 사용), 세미 이후에 적용

---

## 3. 기술 스택 및 아키텍처

### 3.1 기술 스택

#### 백엔드
- **언어**: Java 21 (LTS)
- **프레임워크**: Spring Boot 4.0.0, Spring Cloud 2025.x
- **데이터베이스**: Mysql, Redis 7, Elasticsearch 8
- **메시징**: Apache Kafka 3.x
- **빌드 도구**: Gradle 8.x

#### 인프라 및 DevOps
- **컨테이너**: Docker
- **오케스트레이션**: Kubernetes (K8s)
- **CI/CD**: GitHub Actions
- **컨테이너 레지스트리**: GHCR (GitHub Container Registry)
- **모니터링**: Kibana (향후)

### 3.2 아키텍처

#### 마이크로서비스 아키텍처 (MSA)
```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ API Gateway │
└──────┬──────┘
       │
       ├──► member-service
       ├──► auth-service
       ├──► farm-service
       ├──► product-service
       ├──► order-service
       ├──► payment-service
       ├──► delivery-service
       ├──► notification-service
       ├──► experience-service
       ├──► settlement-service
       ├──► review-service
       ├──► search-service
       └──► cart-service
```

#### 이벤트 기반 아키텍처
```
order-service ──► Kafka ──► payment-service
                          └─► delivery-service
                          └─► notification-service
```

```
notification-service ──► Kafka ──► member-service
                                 └─► farm-service
```

```
product-service ──► Kafka ──► search-service
```

```
settlement-service ──► Spring Batch ──► member-service
```

```
member-service ──► Kafka ──► experience-service
                           └─► review-service
```

```
experience-service ──► Kafka ──► notification-service
```

```
review-service ──► Kafka ──► notification-service
```

### 3.3 데이터베이스 전략

- **서비스별 독립 데이터베이스**: 각 마이크로서비스는 MySql 서버 내 각 데이터베이스 연동
- **CQRS 패턴**: 읽기/쓰기 분리 (검색 서비스는 Elasticsearch 사용)
- **이벤트 소싱**: Kafka를 통한 이벤트 기반 통신

---

## 4. 프로젝트 일정

### 4.1 전체 일정 요약

| 기간 | 주차 | 핵심 목표 | 주요 산출물 |
|------|------|----------|------------|
| 12/1 ~ 12/7 | Week 1 | 인프라 구축 + 인증 + 기본 서비스 | Docker Compose, 공통 라이브러리, 인증 시스템, 프로젝트 문서들 |
| 12/8 ~ 12/15 | Week 2 | 핵심 기능 개발 + 통합 | 주문-결제-배송 플로우, 검색 기능 |
| 12/15 ~ 12/17 | Week 3 | 테스트 + 배포 + 시연 준비 | EC2 배포, 시연 시나리오 |

### 4.2 상세 일정

#### Week 1: 기반 구축 (12/1 ~ 12/7)

**목표**
- ✅ 인프라 구축 (Docker Compose)
- ✅ 공통 라이브러리 개발
- ✅ 인증 시스템 완성
- ✅ 기본 서비스 구조 완성

**주요 작업**
- Day 1-2: 프로젝트 킥오프, 프로젝트 문서작성 
- Day 3-4: 인증 서비스 개발 (70%), 기본 서비스 개발, Docker Compose 환경 구축
- Day 5-7: 인증 서비스 완성, 코드 리뷰, 개별 테스트

#### Week 2: 핵심 기능 개발 (12/8 ~ 12/14)

**목표**
- ✅ 주문 → 결제 → 배송 플로우 완성
- ✅ 검색 기능 완성
- ✅ Kafka 이벤트 통합
- ✅ 통합 테스트

**주요 작업**
- Day 8-10: 주문 서비스, 결제 서비스, 배송 서비스, 
- Day 11-13: 결제 서비스 완성, 통합 테스트, 버그 수정
- Day 14: 최종 통합 테스트, API 문서 업데이트

#### Week 3: 최종 마무리 (12/15 ~ 12/17)

**목표**
- ✅ 최종 테스트
<!-- - ✅ Kubernetes 배포 -->
- ✅ 시연 준비
- ✅ 문서 정리

**주요 작업**
- Day 15: 최종 테스트, 최종 코드 리뷰
- Day 16: 시스템 안정성 검증, 배포 준비
- Day 17: 최종 배포, 배포 검증, 시연 리허설, 발표

---

## 5. 팀 역할 및 책임

### 5.1 팀 구성

| 역할 | 담당자 | 주요 책임 | 담당 서비스 |
|------|--------|----------|------------|
| **🧭 PO** | - | 프로젝트 관리 + DevOps + 개발 | CI/CD, K8s, 인프라, experience-service, front-end |
| **🧱 아키텍트** | - | MSA 설계 + 개발 | auth-service, member-service, api-gateway, notification-service |
| **🧑‍💻 백엔드1** | - | 핵심 API 개발 | farm-service, order-service, payment-service |
| **🧑‍💻 백엔드2** | - | 비즈니스 로직 및 핵심 API 개발 | product-service, delivery-service, settlement-service, review-service |
| **🧠 AI** | - | AI + 검색 | search-service, cart-service |

### 5.2 역할별 상세 책임

#### PO (Product Owner)
- 프로젝트 일정 관리 및 리스크 관리
- GitHub Actions CI/CD 파이프라인 구축
- Kubernetes 클러스터 설정 및 배포 관리
- 예약 관리 서비스 개발
- 통계 서비스 개발
- Front-end 개발

#### 아키텍트
- 전체 시스템 아키텍처 설계
- 인증/인가 시스템 개발
- API Gateway 구축
- 코드 리뷰 및 기술 지원

#### 백엔드 개발자 1
- 농장 관리 서비스 개발
- 주문 관리 서비스 개발 (Saga 패턴)
- 결제 관리 서비스 개발 (Mock PG 연동)
- Kafka 이벤트 발행/구독

#### 백엔드 개발자 2
- 상품 관리 서비스 개발
- 배송 관리 서비스 개발
- 정산 관리 (동시성 제어)
- 리뷰 관리 서비스 개발

#### AI 개발자
- 검색 서비스 개발 (Elasticsearch)
- 장바구니 서비스 개발 (Redis)
- 검색 성능 최적화
- 자동완성 기능 구현

---

## 6. CI/CD 파이프라인

### 6.1 파이프라인 개요

바로팜 프로젝트는 **GitHub Actions**를 기반으로 한 자동화된 CI/CD 파이프라인을 구축합니다.

```
[개발자 코드 푸시]
    │
    ▼
[GitHub Repository]
    │
    ▼
[GitHub Actions]
    │
    ├─► [빌드 및 테스트]
    │   └─► Gradle 빌드
    │   └─► 단위 테스트 실행
    │
    ├─► [Docker 이미지 빌드]
    │   └─► 변경된 서비스만 빌드
    │   └─► Multi-stage build
    │
    ├─► [GHCR에 푸시]
    │   └─► ghcr.io/owner/service-name:tag
    │
    └─► [Kubernetes 배포]
        └─► kubectl set image
        └─► 롤링 업데이트
        └─► Health check
```

### 6.2 워크플로우 구성

#### 1. Build and Test (`build-and-test.yml`)
- **트리거**: `main`, `develop` 브랜치 푸시, Pull Request
- **작업**: 
  - JDK 17 설정
  - Gradle 빌드
  - 테스트 실행
  - 테스트 결과 업로드

#### 2. Build and Push Docker (`build-and-push-docker.yml`)
- **트리거**: `services/**` 경로 변경, 수동 실행
- **작업**:
  - 변경된 서비스 감지
  - Docker 이미지 빌드 (Buildx)
  - GHCR에 푸시
  - 이미지 태그: `{branch}-{sha}`, `latest`

#### 3. Deploy to Kubernetes (`deploy-to-k8s.yml`)
- **트리거**: `main` 브랜치 푸시, 수동 실행
- **작업**:
  - kubectl 설정
  - Kubernetes 클러스터 연결 확인
  - Deployment 이미지 업데이트
  - 롤링 업데이트 대기
  - 배포 상태 확인
  - 실패 시 자동 롤백

#### 4. Complete CI/CD Pipeline (`ci-cd-complete.yml`)
- **트리거**: `main` 브랜치 푸시
- **작업**: 전체 파이프라인 통합 실행

### 6.3 GitHub 설정

#### 필수 Secrets
- `KUBE_CONFIG`: Kubernetes 클러스터 kubeconfig (base64 인코딩)

#### 자동 제공
- `GITHUB_TOKEN`: GHCR 인증용 (자동 제공)

#### Variables (선택사항)
- `CONTAINER_REGISTRY`: `ghcr.io` (기본값)
- `K8S_NAMESPACE`: `baro-farm` (기본값)

---

## 7. 배포 전략

### 7.1 배포 환경

#### 개발 환경 (Development)
- **목적**: 로컬 개발 및 테스트
- **도구**: Docker Compose
- **데이터베이스**: 로컬 PostgreSQL, Redis, Kafka

<!-- #### 스테이징 환경 (Staging)
- **목적**: 통합 테스트 및 검증
- **도구**: Kubernetes
- **네임스페이스**: `baro-farm-staging`
- **배포**: `develop` 브랜치 자동 배포 -->

#### 운영 환경 (Production)
- **목적**: 실제 서비스 운영
- **도구**: Docker-Compose
- **네임스페이스**: `baro-farm`
- **배포**: `main-<모듈명>` 브랜치 PR 후 배포

### 7.2 배포 전략

#### 롤링 업데이트 (Rolling Update)
- **방식**: 기존 Pod를 점진적으로 새 Pod로 교체
- **장점**: 무중단 배포 가능
- **설정**: Kubernetes Deployment의 기본 전략

#### Health Check
- **Liveness Probe**: Pod가 살아있는지 확인
- **Readiness Probe**: 트래픽을 받을 준비가 되었는지 확인
- **Startup Probe**: 애플리케이션 시작 시간 고려

#### 롤백 전략
- **자동 롤백**: Health check 실패 시 자동 롤백
- **수동 롤백**: `kubectl rollout undo` 명령어 사용
- **히스토리 관리**: Deployment revision 관리

### 7.3 Kubernetes 리소스

#### Namespace
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: baro-farm
```

#### Deployment
- 각 서비스별 Deployment 생성
- Replica: 2개 (운영 환경에서는 3개 이상 권장)
- Resource Limits: CPU 500m, Memory 512Mi

#### Service
- ClusterIP 타입 사용
- 내부 통신용

#### ConfigMap & Secret
- 환경별 설정 분리
- 민감 정보는 Secret 사용

### 7.4 배포 프로세스

1. **코드 푸시**: 개발자가 `main-<모듈명>` 브랜치에 푸시
2. **자동 빌드**: GitHub Actions가 자동으로 빌드 및 테스트 실행
3. **Docker 이미지 빌드**: 변경된 서비스의 Docker 이미지 빌드
4. **GHCR 푸시**: 빌드된 이미지를 GHCR에 푸시
5. **Kubernetes 배포**: 새 이미지로 Deployment 업데이트
6. **롤링 업데이트**: 기존 Pod를 새 Pod로 점진적 교체
7. **검증**: Health check 및 배포 상태 확인

---

## 8. 리스크 관리

### 8.1 기술적 리스크

| 리스크 | 영향도 | 확률 | 대응 방안 |
|--------|--------|------|----------|
| **Kafka 통합 복잡도** | 높음 | 중간 | Week 2에 Kafka 환경 확실히 구축, 간단한 이벤트부터 시작 |
<!-- | **Kubernetes 배포 실패** | 높음 | 낮음 | 로컬에서 사전 테스트, 롤백 전략 수립 | -->
| **서비스 간 통신 오류** | 중간 | 중간 | Circuit Breaker 패턴 적용, 타임아웃 설정 |
| **데이터베이스 성능** | 중간 | 낮음 | 인덱스 최적화, 쿼리 튜닝 |

### 8.2 일정 리스크

| 리스크 | 영향도 | 확률 | 대응 방안 |
|--------|--------|------|----------|
| **일정 지연** | 높음 | 중간 | 데일리스크럼, 블로커 조기 제거, 범위 조정 유연성 |
| **팀원 일정 충돌** | 높음 | 낮음 | 주간 일정 점검 |

### 8.3 비즈니스 리스크

| 리스크 | 영향도 | 확률 | 대응 방안 |
|--------|--------|------|----------|
| **요구사항 변경** | 중간 | 낮음 | 명확한 범위 정의, 변경 요청 프로세스 수립 |
| **기술 부채 누적** | 중간 | 중간 | 코드 리뷰 강화, 리팩토링 시간 확보 |

---

## 9. 성공 기준

### 9.1 기술적 성공 기준

#### 필수 (Must Have)
- ✅ 모든 핵심 서비스 정상 동작
- ✅ 인증/인가 시스템 완성
- ✅ 주문 → 결제 → 배송 플로우 완성
- ✅ Kafka 이벤트 플로우 동작
- ✅ CI/CD 파이프라인 구축
- ✅ Kubernetes 배포 성공
- ✅ 시연 가능한 수준

#### 기대 (Should Have)
- ✅ 9개 서비스 완성
- ✅ 검색 기능 동작
- ✅ 장바구니 기능 동작
- ✅ 통합 테스트 통과
- ✅ API 문서화 완료

#### 추가 (Nice to Have)
- ✅ 모니터링 시스템 구축
- ✅ 로깅 시스템 구축
- ✅ 성능 최적화
- ✅ 보안 강화

### 9.2 비즈니스 성공 기준

- ✅ 시연 시나리오 완벽 실행
- ✅ 핵심 기능 시연 가능
- ✅ 안정적인 시스템 운영
- ✅ 확장 가능한 아키텍처

### 9.3 품질 기준

- ✅ 코드 커버리지 70% 이상
- ✅ API 응답 시간 500ms 이하
- ✅ 시스템 가용성 99% 이상
- ✅ 무중단 배포 가능

---

## 10. 문서화

### 10.1 필수 문서

- [x] README.md
- [x] 프로젝트 개요 (01_PROJECT_OVERVIEW.md)
- [x] 도메인 분석 (02_DOMAIN_ANALYSIS.md)
- [x] 아키텍처 다이어그램 (03_ARCHITECTURE_DIAGRAM.md)
- [x] API 명세서 (04_API_SPECIFICATION.md)
- [x] 프로젝트 구조 (05_PROJECT_STRUCTURE.md)
- [x] 프로젝트 계획서 (12_PROJECT_PLAN.md)

### 10.2 추가 문서

- [ ] 배포 가이드
- [ ] 시연 스크립트
- [ ] 트러블슈팅 가이드
- [ ] API 사용 가이드

---

## 11. 다음 단계 (최종)

### 11.1 확장 기능

- experience-service (체험 예약)
- analytics-service (통계)
- notification-service (알림)

### 11.2 인프라 고도화

- 모니터링 시스템 구축 (Prometheus, Grafana)
- 로깅 시스템 구축 (ELK Stack)
- 분산 추적 (Zipkin/Jaeger)

### 11.3 성능 최적화

- 데이터베이스 최적화
- 캐싱 전략 개선
- CDN 도입
- 로드 밸런싱 고도화

---

*최종 업데이트: 2025년 12월 03일*

