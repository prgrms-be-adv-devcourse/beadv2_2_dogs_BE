# Docker 이미지 버전 관리 가이드

Docker 이미지의 버전 관리, 롤백, 정리 방법을 설명합니다.

## 📋 목차

- [태그 전략](#태그-전략)
- [버전 확인](#버전-확인)
- [롤백 방법](#롤백-방법)
- [자동 정리](#자동-정리)
- [배포 이력 추적](#배포-이력-추적)

---

## 태그 전략

### 자동 생성되는 태그

GitHub Actions가 자동으로 여러 개의 태그를 생성합니다:

```
1. latest                           ← 가장 최신 버전
2. main-auth                        ← 브랜치명
3. main-auth-abc123                 ← 브랜치-커밋SHA (짧은 형식)
4. main-auth-20251205-143022        ← 브랜치-타임스탬프
```

### 실제 예시

```bash
# 2025-12-05 14:30에 main-auth 브랜치에 Push (커밋 SHA: abc123def)
# → 다음 이미지들이 생성됨:

ghcr.io/do-develop-space/baro-auth:latest
ghcr.io/do-develop-space/baro-auth:main-auth
ghcr.io/do-develop-space/baro-auth:main-auth-abc123d
ghcr.io/do-develop-space/baro-auth:main-auth-20251205-143022
```

---

## 버전 확인

### 방법 1: 스크립트 사용 (권장)

```bash
# EC2에서 실행
bash list-versions.sh auth

# 출력 예시:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📦 Available versions for: baro-auth
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🖥️  Local images:
TAG                          SIZE      CREATED
latest                       200MB     2 hours ago
main-auth-abc123d            200MB     2 hours ago
main-auth-20251205-143022    200MB     2 hours ago
main-auth-def456e            195MB     1 day ago

📋 Recent deployments:
[2025-12-05 14:30:22] Deploy: auth | New: ...main-auth-abc123d
[2025-12-04 09:15:33] Deploy: auth | New: ...main-auth-def456e

🏃 Currently running:
Image: ghcr.io/.../baro-auth:main-auth-abc123d
Started: 2025-12-05T14:30:25Z

🔄 Rollback example:
bash rollback.sh auth main-auth-def456e
```

### 방법 2: GitHub UI

```
1. GitHub 레포지토리 → Packages
2. baro-auth 클릭
3. 모든 버전 확인 (태그별)
```

### 방법 3: Docker 명령어

```bash
# 로컬 이미지 확인
docker images ghcr.io/do-develop-space/baro-auth

# 출력:
REPOSITORY                                    TAG                        IMAGE ID
ghcr.io/.../baro-auth                        latest                     abc123
ghcr.io/.../baro-auth                        main-auth-abc123d          abc123
ghcr.io/.../baro-auth                        main-auth-20251205-143022  abc123
```

---

## 롤백 방법

### 1. 롤백 스크립트 사용 (권장)

```bash
# 1. 사용 가능한 버전 확인
bash list-versions.sh auth

# 2. 이전 버전으로 롤백
bash rollback.sh auth main-auth-def456e

# 실행 과정:
# ✅ 현재 버전 백업
# ✅ 타겟 이미지 Pull
# ✅ 기존 컨테이너 중지
# ✅ 타겟 버전으로 시작
# ✅ Health Check
# ✅ 성공 시 백업 삭제, 실패 시 자동 복원
```

### 2. 특정 커밋으로 롤백

```bash
# GitHub에서 이전 커밋 SHA 확인
# 예: abc123d

# 해당 SHA로 롤백
bash rollback.sh auth main-auth-abc123d
```

### 3. 타임스탬프로 롤백

```bash
# 특정 시간의 버전으로 롤백
bash rollback.sh auth main-auth-20251205-143022
```

### 4. 수동 롤백

```bash
# IMAGE_TAG 환경 변수로 버전 지정
export IMAGE_TAG=main-auth-def456e
docker-compose -f docker-compose.auth.yml up -d
```

---

## 자동 정리

### GitHub Actions 자동 정리 (권장)

배포 성공 후 자동으로 오래된 이미지 삭제:

```yaml
# .github/workflows/ci-cd.yml
cleanup:
  steps:
    - name: Delete old container images
      uses: actions/delete-package-versions@v4
      with:
        min-versions-to-keep: 5  # 최근 5개만 유지
```

**동작:**
- ✅ 배포 성공 후 자동 실행
- ✅ 최근 5개 버전만 유지
- ✅ 오래된 버전 자동 삭제
- ✅ GitHub Container Registry 용량 관리

### 수동 정리 (EC2)

```bash
# 로컬 이미지 정리
bash cleanup-images.sh 5  # 최근 5개만 유지

# 또는 기본 Docker 명령어
docker image prune -a -f  # 사용하지 않는 모든 이미지 삭제
```

---

## 배포 이력 추적

### 자동 기록

모든 배포는 자동으로 기록됩니다:

```bash
# EC2의 ~/deployment-history.log
cat ~/deployment-history.log

# 출력 예시:
[2025-12-05 14:30:22] Deploy: auth | Previous: ...def456e | New: ...abc123d
[2025-12-05 15:45:10] Deploy: buyer | Previous: ...ghi789j | New: ...klm012n
[2025-12-05 16:20:33] Rollback: auth from ...abc123d to ...def456e
```

### 이력 조회

```bash
# 특정 모듈 이력
grep "auth" ~/deployment-history.log

# 최근 10개 배포
tail -10 ~/deployment-history.log

# 오늘 배포 이력
grep "$(date '+%Y-%m-%d')" ~/deployment-history.log
```

---

## 실전 예시

### 시나리오 1: 버그 발견 후 롤백

```bash
# 1. 버그 발견!
# 2025-12-05 14:30에 배포한 버전에 문제 발생

# 2. EC2 접속
ssh -i key.pem ubuntu@ec2-ip

# 3. 사용 가능한 버전 확인
bash list-versions.sh auth
# → main-auth-abc123d (현재, 문제 있음)
# → main-auth-def456e (이전, 정상)

# 4. 이전 버전으로 롤백
bash rollback.sh auth main-auth-def456e

# 5. 확인
curl http://localhost:8081/actuator/health
# → 정상 작동!

# 소요 시간: 약 30초
```

---

### 시나리오 2: 주간 정기 정리

```bash
# EC2에서 매주 실행 (또는 Cron Job 등록)
bash cleanup-images.sh 10  # 최근 10개 버전만 유지

# Cron Job 등록 예시
# 매주 일요일 새벽 3시에 자동 정리
crontab -e
0 3 * * 0 /home/ubuntu/cleanup-images.sh 10 >> /home/ubuntu/cleanup.log 2>&1
```

---

### 시나리오 3: 버전 비교

```bash
# 1. 배포 이력 확인
cat ~/deployment-history.log | grep "auth"

# 2. 특정 두 버전 비교 (GitHub UI)
# GitHub → Actions → 각 배포 확인
# 커밋 diff 확인

# 3. 안전한 버전 선택
bash rollback.sh auth main-auth-abc123d
```

---

## 📊 버전 보관 정책

### GitHub Container Registry

```
자동 정리 정책 (GitHub Actions):
├── 최근 5개 버전 유지
├── 오래된 버전 자동 삭제
└── 용량 관리 자동화
```

### EC2 로컬 이미지

```
수동 정리 (cleanup-images.sh):
├── 최근 N개 버전 유지 (기본 5개)
├── 미사용 이미지 삭제
└── 디스크 공간 확보
```

---

## 🔧 스크립트별 용도

| 스크립트 | 용도 | 실행 주기 | 자동/수동 |
|---------|------|----------|----------|
| `deploy-module.sh` | 모듈 배포 | 배포 시 | 자동 + 수동 |
| `rollback.sh` | 버전 롤백 | 문제 발생 시 | 주로 수동 |
| `list-versions.sh` | 버전 조회 | 필요 시 | 수동 |
| `cleanup-images.sh` | 이미지 정리 | 주간 | 수동 (Cron 가능) |

---

## 🎯 Best Practices

### 1. 배포 전

```bash
# 현재 버전 확인
bash list-versions.sh auth

# 배포 이력 확인
tail ~/deployment-history.log
```

### 2. 배포 후

```bash
# Health Check
curl http://localhost:8081/actuator/health

# 로그 확인
docker-compose -f docker-compose.auth.yml logs -f --tail=100

# 문제 있으면 즉시 롤백
bash rollback.sh auth main-auth-def456e
```

### 3. 정기 관리

```bash
# 주간 이미지 정리
bash cleanup-images.sh 10

# 월간 배포 이력 백업
cp ~/deployment-history.log ~/deployment-history-backup-$(date +%Y%m).log
```

---

## 🚨 긴급 롤백 절차

```bash
# 1. EC2 즉시 접속
ssh -i key.pem ubuntu@ec2-ip

# 2. 이전 버전 확인 (최근 배포 이력)
tail ~/deployment-history.log
# → [2024-12-05 14:30:22] Deploy: auth | Previous: ...def456e

# 3. 즉시 롤백
bash rollback.sh auth main-auth-def456e

# 4. 확인
docker ps | grep auth
curl http://localhost:8081/actuator/health

# 총 소요 시간: 1분 이내
```

---

## 📈 모니터링

### 디스크 사용량 확인

```bash
# Docker 전체 용량
docker system df

# 출력:
TYPE            TOTAL     ACTIVE    SIZE      RECLAIMABLE
Images          25        8         5.5GB     2.1GB (38%)
Containers      8         8         100MB     0B (0%)
Local Volumes   4         4         1.2GB     0B (0%)
```

### 이미지 개수 확인

```bash
# 모듈별 이미지 개수
for module in auth buyer seller order support; do
  count=$(docker images "ghcr.io/do-develop-space/baro-${module}" | wc -l)
  echo "baro-${module}: $((count - 1)) versions"
done

# 출력:
baro-auth: 7 versions
baro-buyer: 5 versions
baro-seller: 6 versions
```

---

## 🔄 전체 워크플로우

### 배포 → 백업 → 롤백

```
┌──────────────────────────────────────┐
│  1. 배포 (자동)                       │
│  git push origin main-auth           │
│  → GitHub Actions 실행                │
│  → 새 이미지 생성 (4개 태그)          │
│  → EC2 자동 배포                      │
│  → deployment-history.log 기록        │
└─────────────┬────────────────────────┘
              │
              ↓
┌──────────────────────────────────────┐
│  2. 백업 (자동)                       │
│  → 이전 이미지 GHCR에 보관            │
│  → 최근 5개 버전 유지                 │
│  → 오래된 버전 자동 삭제              │
└─────────────┬────────────────────────┘
              │
              ↓ (문제 발생 시)
┌──────────────────────────────────────┐
│  3. 롤백 (수동)                       │
│  bash list-versions.sh auth          │
│  bash rollback.sh auth [이전_태그]    │
│  → Health Check 자동 실행             │
│  → 실패 시 자동 복원                  │
└──────────────────────────────────────┘
```

---

## 📝 스크립트 사용법 요약

### 버전 확인

```bash
bash list-versions.sh auth      # Auth 모듈 버전 목록
bash list-versions.sh buyer     # Buyer 모듈 버전 목록
```

### 롤백

```bash
bash rollback.sh auth main-auth-def456e              # 커밋 SHA로
bash rollback.sh buyer main-buyer-20251205-143022    # 타임스탬프로
```

### 정리

```bash
bash cleanup-images.sh 5    # 최근 5개만 유지
bash cleanup-images.sh 10   # 최근 10개만 유지
```

---

## 🎯 자동화 레벨

| 작업 | 자동화 레벨 | 설명 |
|------|-----------|------|
| **이미지 빌드** | 완전 자동 | Git Push 시 자동 |
| **이미지 태깅** | 완전 자동 | 4가지 태그 자동 생성 |
| **배포** | 완전 자동 | main-* Push 시 자동 |
| **이력 기록** | 완전 자동 | 배포 시 자동 기록 |
| **이미지 정리** | 완전 자동 | 배포 후 자동 실행 |
| **롤백** | 수동 | 사람이 판단 후 실행 |
| **버전 확인** | 수동 | 필요 시 스크립트 실행 |

---

## 💡 핵심 요약

### 자동으로 관리되는 것

- ✅ **이미지 빌드**: main-* Push 시 자동
- ✅ **버전 태깅**: 4가지 태그 자동 생성
- ✅ **배포 이력**: deployment-history.log 자동 기록
- ✅ **오래된 이미지 정리**: 최근 5개만 유지

### 수동으로 하는 것

- 🔧 **롤백**: 문제 판단 후 수동 실행
- 🔧 **버전 확인**: 필요 시 스크립트 실행
- 🔧 **긴급 배포**: EC2에서 직접 실행

---

## 🚀 실전 팁

### 안전한 배포 체크리스트

```bash
# 1. 배포 전: 현재 버전 기록
bash list-versions.sh auth > ~/pre-deploy-auth.txt

# 2. 배포 (자동 또는 수동)
git push origin main-auth

# 3. 배포 후: Health Check (5분 모니터링)
for i in {1..10}; do
  curl http://localhost:8081/actuator/health
  sleep 30
done

# 4. 문제 발생 시: 즉시 롤백
bash rollback.sh auth [이전_버전]
```

---

## 📚 참고 명령어 모음

```bash
# 버전 관리
bash list-versions.sh [module]           # 버전 목록
bash rollback.sh [module] [tag]          # 롤백
bash cleanup-images.sh [keep_count]      # 정리

# 이력 확인
cat ~/deployment-history.log             # 전체 이력
grep "auth" ~/deployment-history.log     # 모듈별 이력
tail -20 ~/deployment-history.log        # 최근 20개

# 현재 상태
docker ps                                # 실행 중인 컨테이너
docker images | grep baro                # 로컬 이미지
docker system df                         # 디스크 사용량
```

모든 버전 관리가 **자동화 + 수동 제어** 조합으로 안전하게 구성되었습니다! 🎉
