# 📦 프로젝트 구조

```
baro-farm/
├── baro-auth/                    # A. 인증 모듈
│   └── auth-service              # 인증/인가 서비스 (JWT)
│
├── baro-buyer/                   # B. 구매자 모듈
│   ├── buyer-service             # 구매자 관리
│   ├── cart-service              # 장바구니
│   └── product-service           # 상품 관리
│
├── baro-seller/                  # C. 판매자 모듈
│   ├── seller-service            # 판매자 관리
│   └── farm-service              # 농장 관리
│
├── baro-order/                   # D. 주문 모듈
│   ├── order-service             # 주문 관리
│   └── payment-service           # 결제 처리
│
├── baro-support/                 # E. 지원 모듈
│   ├── settlement-service        # 정산 관리
│   ├── delivery-service          # 배송 관리
│   ├── notification-service      # 알림 서비스
│   ├── experience-service        # 체험 프로그램
│   ├── search-service            # 검색 서비스
│   └── review-service            # 리뷰 관리
│
└── baro-cloud/                   # F. Spring Cloud 모듈
    ├── gateway-service           # API Gateway
    ├── config-server             # 설정 서버
    └── eureka-server             # 서비스 디스커버리
```


---

*최종 업데이트: 2025년 12월 03일*