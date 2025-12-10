# 바로팜 프로젝트 구조

## 📦 모듈 구조 (모듈러 모놀리스)

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
├── baro-cloud/                   # F. 인프라 모듈 (개별 배포)
│   ├── gateway/
│   │   ├── src/main/java/com/barofarm/gateway/
│   │   │   └── GatewayApplication.java
│   │   └── build.gradle
│   ├── config/
│   │   ├── src/main/java/com/barofarm/config/
│   │   │   └── ConfigApplication.java
│   │   └── build.gradle
│   └── eureka/
│       ├── src/main/java/com/barofarm/eureka/
│       │   └── EurekaApplication.java
│       └── build.gradle
│
├── config/checkstyle/            # 코드 품질 설정
│   ├── checkstyle.xml
│   └── suppressions.xml
├── scripts/                      # Git Hooks
│   ├── pre-commit
│   └── install-hooks.sh
├── build.gradle                  # Root Gradle 설정
├── settings.gradle
└── README.md
```

## 🎯 아키텍처 특징

### 모듈러 모놀리스 (Modular Monolith)

- **하나의 모듈 = 하나의 JAR 파일**
- **모듈 내부는 패키지로 도메인 분리**
- **같은 모듈 내 도메인 간 메서드 호출**
- **다른 모듈 간에는 Feign으로 통신**

### 배포 단위

| 모듈 | JAR 파일 | 포트 | 포함 도메인 |
|------|---------|------|------------|
| baro-auth | baro-auth.jar | 8081 | auth |
| baro-buyer | baro-buyer.jar | 8082 | buyer, cart, product |
| baro-seller | baro-seller.jar | 8085 | seller, farm |
| baro-order | baro-order.jar | 8087 | order, payment |
| baro-support | baro-support.jar | 8089 | settlement, delivery, notification, experience, search, review |
| gateway | gateway.jar | 8080 | API Gateway |
| config | config.jar | 8888 | Config Server |
| eureka | eureka.jar | 8761 | Service Registry |

## 🔄 통신 방식

### 모듈 내부 (같은 JAR)
```java
// baro-buyer.jar 내부
@Service
class CartService {
    @Autowired
    private ProductService productService; // 메서드 호출
}
```

### 모듈 간 (다른 JAR)
```java
// baro-order.jar → baro-buyer.jar
@FeignClient("buyer-service")
interface ProductClient {
    @GetMapping("/products/{id}")
    Product getProduct(@PathVariable Long id); // HTTP 통신
}
```

## 📊 의존성 흐름

```
Gateway (8080)
    ↓
┌───────────────────────────────────┐
│  Eureka Server (8761)             │
└───────────────────────────────────┘
    ↓
┌───────────┬───────────┬───────────┐
│ baro-auth │baro-buyer │baro-seller│
│  (8081)   │  (8082)   │  (8085)   │
└───────────┴───────────┴───────────┘
         ↓          ↓
    ┌─────────┬─────────────┐
    │baro-order│baro-support│
    │ (8087)   │   (8089)   │
    └─────────┴─────────────┘
```

## 🚀 실행 방법

```bash
# 1. Eureka Server 실행
java -jar baro-cloud/eureka/build/libs/eureka-0.0.1-SNAPSHOT.jar

# 2. Config Server 실행
java -jar baro-cloud/config/build/libs/config-0.0.1-SNAPSHOT.jar

# 3. 비즈니스 모듈 실행
java -jar baro-auth/build/libs/baro-auth-0.0.1-SNAPSHOT.jar
java -jar baro-buyer/build/libs/baro-buyer-0.0.1-SNAPSHOT.jar
java -jar baro-seller/build/libs/baro-seller-0.0.1-SNAPSHOT.jar
java -jar baro-order/build/libs/baro-order-0.0.1-SNAPSHOT.jar
java -jar baro-support/build/libs/baro-support-0.0.1-SNAPSHOT.jar

# 4. Gateway 실행
java -jar baro-cloud/gateway/build/libs/gateway-0.0.1-SNAPSHOT.jar
```

## 🎨 향후 MSA 전환 시

모듈러 모놀리스에서 MSA로 전환하려면:

1. 각 도메인 패키지를 독립 모듈로 분리
2. 내부 메서드 호출을 Feign으로 변경
3. 각각 독립 배포

```
baro-buyer.jar (3개 도메인)
    ↓ 분리
buyer-service.jar
cart-service.jar
product-service.jar
```
