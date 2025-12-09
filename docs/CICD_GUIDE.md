# CI/CD 가이드

Baro Farm 프로젝트의 GitHub Actions 기반 CI/CD 파이프라인 구축 가이드입니다.

## 📋 목차

- [CI/CD 개요](#cicd-개요)
- [파이프라인 구조](#파이프라인-구조)
- [사전 준비사항](#사전-준비사항)
- [GitHub Secrets 설정](#github-secrets-설정)
- [AWS EC2 설정](#aws-ec2-설정)
- [배포 프로세스](#배포-프로세스)
- [트러블슈팅](#트러블슈팅)

---

## CI/CD 개요

### 사용 기술

- **CI/CD**: GitHub Actions
- **컨테이너**: Docker
- **레지스트리**: GitHub Container Registry (GHCR)
- **배포 환경**: AWS EC2
- **오케스트레이션**: Docker Compose

### 자동화 범위

```
Code Push → CI (빌드/테스트) → Docker Image Build → 
Docker Hub Push → EC2 Deploy → Health Check
```

---

## 파이프라인 구조

### 1. CI (Continuous Integration)

**트리거:**
- `main`, `main-*`, `dev-*` 브랜치에 Push
- Pull Request 생성

**작업:**
1. ✅ 코드 포맷 검사 (Spotless)
2. ✅ 코드 스타일 검사 (Checkstyle)
3. ✅ Gradle 빌드
4. ✅ 단위 테스트 실행
5. ✅ 빌드 아티팩트 저장

### 2. Docker Build

**트리거:**
- `main` 또는 `main-*` 브랜치에 Push

**작업:**
1. ✅ Docker 이미지 빌드
2. ✅ GitHub Container Registry에 Push
3. ✅ 이미지 태그: `latest`, `{branch}-{sha}`

**빌드 대상 서비스:**
- baro-auth
- baro-buyer
- baro-seller
- baro-order
- baro-support
- eureka
- gateway
- config

### 3. CD (Continuous Deployment)

**트리거:**
- `main` 브랜치에 Push (프로덕션 배포)

**작업:**
1. ✅ EC2에 배포 스크립트 전송
2. ✅ EC2에서 최신 이미지 Pull
3. ✅ 기존 컨테이너 중지
4. ✅ 새 컨테이너 시작
5. ✅ Health Check

---

## 사전 준비사항

### 1. GitHub Container Registry 설정

**GitHub에서 자동으로 이미지 저장소가 생성됩니다.**

```bash
# 이미지는 다음 형식으로 저장됩니다:
ghcr.io/{github-username}/{service-name}:tag

# 예시:
ghcr.io/do-develop-space/baro-auth:latest
ghcr.io/do-develop-space/baro-buyer:latest
ghcr.io/do-develop-space/eureka:latest
...
```

**이미지 공개 설정 (자동):**
✅ **CI/CD 파이프라인에서 자동으로 패키지를 public으로 설정합니다!**

이미지를 push한 후 자동으로 GitHub API를 통해 패키지 visibility를 public으로 변경합니다.
따라서 별도로 수동 설정할 필요가 없습니다.

**수동 설정이 필요한 경우:**
만약 자동 설정이 실패하거나 수동으로 변경하려면:

1. GitHub → https://github.com/users/do-develop-space/packages
2. 변경할 패키지 클릭 (예: `baro-support`)
3. 우측 하단의 **"Package settings"** 클릭
4. **"Change visibility"** 또는 **"Danger Zone"** 섹션에서 **"Change visibility"** 클릭
5. **"Make public"** 선택
6. 패키지 이름 입력하여 확인

**참고:**
- ✅ Public 패키지는 인증 없이 pull 가능
- ⚠️ Private 패키지는 `GHCR_PAT` 또는 `GITHUB_TOKEN` 필요
- ✅ 모든 서비스 패키지가 자동으로 public으로 설정됨

### 2. AWS EC2 인스턴스

**최소 사양:**
- Type: t3.medium (2 vCPU, 4GB RAM) 이상
- OS: Ubuntu 22.04 LTS
- Storage: 30GB 이상
- Security Group: 8080-8089, 8761, 8888 포트 오픈

### 3. EC2에 Docker 설치

```bash
# EC2에 SSH 접속
ssh -i your-key.pem ubuntu@your-ec2-ip

# Docker 설치
sudo apt update
sudo apt install -y docker.io docker-compose
sudo systemctl start docker
sudo systemctl enable docker

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER
newgrp docker

# Docker 버전 확인
docker --version
docker-compose --version
```

---

## GitHub Secrets 설정

GitHub 레포지토리 → Settings → Secrets and variables → Actions → New repository secret

### 필수 Secrets

| Secret 이름 | 설명 | 예시 | 필요 여부 |
|-------------|------|------|----------|
| `GITHUB_TOKEN` | GitHub Actions 기본 제공 | 자동 생성 | **자동** ✅ |
| `EC2_HOST` | EC2 인스턴스 Public IP | `3.35.123.456` | **필수** |
| `EC2_USERNAME` | EC2 SSH 사용자명 | `ubuntu` | **필수** |
| `EC2_SSH_KEY` | EC2 SSH Private Key | `-----BEGIN RSA PRIVATE KEY-----\n...` | **필수** |

### 주요 특징

**`GITHUB_TOKEN`은 자동 제공됩니다!**
- GitHub Actions가 자동으로 생성 및 관리
- Docker Hub 계정이나 Access Token 불필요
- GitHub Container Registry 접근 권한 자동 부여
- 별도 설정 없이 바로 사용 가능

### EC2 SSH Key 설정

```bash
# EC2 생성 시 받은 .pem 파일 내용 복사
cat your-key.pem

# GitHub Secret EC2_SSH_KEY에 전체 내용 붙여넣기
-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEA...
... (전체 내용)
-----END RSA PRIVATE KEY-----
```

---

## AWS EC2 설정

### 1. Security Group 설정

**Inbound Rules:**

| Type | Protocol | Port Range | Source | Description |
|------|----------|-----------|--------|-------------|
| SSH | TCP | 22 | My IP | SSH 접속 |
| Custom TCP | TCP | 8080 | 0.0.0.0/0 | Gateway |
| Custom TCP | TCP | 8761 | 0.0.0.0/0 | Eureka Dashboard |
| Custom TCP | TCP | 8081-8089 | 0.0.0.0/0 | 비즈니스 서비스 |

### 2. EC2 초기 설정

```bash
# EC2 접속
ssh -i your-key.pem ubuntu@your-ec2-ip

# 시스템 업데이트
sudo apt update && sudo apt upgrade -y

# Docker 설치
sudo apt install -y docker.io docker-compose

# Docker 서비스 시작
sudo systemctl start docker
sudo systemctl enable docker

# 사용자 권한 설정
sudo usermod -aG docker ubuntu
newgrp docker

# 작업 디렉토리 생성
mkdir -p ~/apps/BE
cd ~/apps/BE

# 환경 변수 파일 생성 (선택사항)
cat > .env << EOF
DOCKER_REGISTRY=ghcr.io/do-develop-space
IMAGE_TAG=latest
REDIS_PASSWORD=your-redis-password
GITHUB_TOKEN=your-github-pat  # Personal Access Token (ghcr.io 로그인용)
EOF
```

### 4. GitHub Personal Access Token 생성 (EC2용)

EC2에서 Private 이미지를 Pull하려면 PAT가 필요합니다:

```bash
# GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
1. Generate new token (classic)
2. Note: EC2 Deployment
3. Expiration: 90 days (또는 원하는 기간)
4. Select scopes: 
   - ✅ read:packages (패키지 읽기)
   - ✅ write:packages (패키지 쓰기, 필요시)
5. Generate token
6. 토큰 복사 (다시 볼 수 없음!)

# EC2에 환경 변수로 저장
echo "export GITHUB_TOKEN=ghp_xxxxxxxxxxxx" >> ~/.bashrc
source ~/.bashrc
```

### 3. 배포 스크립트 실행 권한 부여

```bash
chmod +x ~/deploy.sh
```

---

## 배포 프로세스

### 자동 배포 (GitHub Actions)

```bash
# 1. 코드 변경 후 커밋
git add .
git commit -m "[Deploy] 주문 기능 추가"

# 2. main 브랜치에 Push
git push origin main

# 3. GitHub Actions 자동 실행
- CI: 빌드 및 테스트
- Docker Build: 이미지 빌드 및 Push
- Deploy: EC2에 자동 배포

# 4. 배포 확인
GitHub Actions 탭에서 워크플로우 진행 상황 확인
```

### 수동 배포 (EC2에서 직접)

```bash
# EC2 접속
ssh -i your-key.pem ubuntu@your-ec2-ip

# 최신 이미지 Pull
cd ~/apps/BE
docker-compose -f docker-compose.{모듈명}.yml pull

# 서비스 재시작
docker-compose -f docker-compose.{모듈명}.yml down
docker-compose -f docker-compose.{모듈명}.yml up -d

# 컨테이너 상태 확인
docker-compose -f docker-compose.{모듈명}.yml ps

# 로그 확인
docker-compose -f docker-compose.{모듈명}.yml logs -f
```

---

## 배포 확인

### 1. 컨테이너 상태 확인

```bash
# EC2에서 실행
docker ps

# 모든 컨테이너가 "Up" 상태여야 함
CONTAINER ID   IMAGE                        STATUS
abc123...      barofarm/eureka:latest       Up 5 minutes
def456...      barofarm/gateway:latest      Up 4 minutes
ghi789...      barofarm/baro-auth:latest    Up 3 minutes
...
```

### 2. Health Check

```bash
# Eureka Dashboard
http://your-ec2-ip:8761

# Gateway Health Check
curl http://your-ec2-ip:8080/actuator/health

# 각 서비스 Health Check
curl http://your-ec2-ip:8081/actuator/health  # Auth
curl http://your-ec2-ip:8082/actuator/health  # Buyer
curl http://your-ec2-ip:8085/actuator/health  # Seller
curl http://your-ec2-ip:8087/actuator/health  # Order
curl http://your-ec2-ip:8089/actuator/health  # Support
```

### 3. 로그 확인

```bash
# 전체 서비스 로그
docker-compose -f docker-compose.prod.yml logs -f

# 특정 서비스 로그
docker-compose -f docker-compose.prod.yml logs -f gateway
docker-compose -f docker-compose.prod.yml logs -f baro-auth

# 최근 100줄만 보기
docker-compose -f docker-compose.prod.yml logs --tail=100
```

---

## 트러블슈팅

### 1. GitHub Actions 빌드 실패

**문제:** Spotless 또는 Checkstyle 실패

```bash
# 로컬에서 검사
./gradlew spotlessCheck
./gradlew checkstyleMain

# 자동 수정
./gradlew spotlessApply

# 재커밋
git add .
git commit -m "[Fix] 코드 포맷 수정"
git push
```

### 2. Docker 이미지 빌드 실패

**문제:** Dockerfile을 찾을 수 없음

```bash
# Dockerfile 경로 확인
ls -la docker/*/Dockerfile

# 필요시 Dockerfile 생성
# 프로젝트 루트의 docker/ 폴더 확인
```

### 3. EC2 배포 실패

**문제:** SSH 연결 실패

```bash
# EC2_SSH_KEY 형식 확인
-----BEGIN RSA PRIVATE KEY-----
...전체 내용...
-----END RSA PRIVATE KEY-----

# EC2 Security Group에서 SSH (22번 포트) 허용 확인
# EC2 인스턴스가 실행 중인지 확인
```

### 4. 컨테이너 시작 실패

**문제:** 컨테이너가 계속 재시작

```bash
# 로그 확인
docker logs baro-gateway

# 일반적인 원인:
# - Eureka 서버 연결 실패
# - Redis/Kafka 연결 실패
# - 환경 변수 누락

# 순서대로 시작
docker-compose -f docker-compose.prod.yml up -d redis kafka
sleep 30
docker-compose -f docker-compose.prod.yml up -d eureka
sleep 30
docker-compose -f docker-compose.prod.yml up -d gateway config
sleep 30
docker-compose -f docker-compose.prod.yml up -d baro-auth baro-buyer baro-seller baro-order baro-support
```

### 5. 메모리 부족

**문제:** EC2 메모리 부족으로 컨테이너 종료

```bash
# 메모리 사용량 확인
free -h
docker stats

# 해결방법:
# 1. EC2 인스턴스 타입 업그레이드 (t3.medium → t3.large)
# 2. 일부 서비스만 실행
# 3. JVM 메모리 설정 조정 (Dockerfile에서)
```

---

## 고급 설정

### 롤링 업데이트

```yaml
# docker-compose.prod.yml에 추가
services:
  baro-auth:
    deploy:
      replicas: 2
      update_config:
        parallelism: 1
        delay: 10s
```

### Blue-Green 배포

```bash
# Blue (현재 운영)
docker-compose -f docker-compose.blue.yml up -d

# Green (새 버전)
docker-compose -f docker-compose.green.yml up -d

# 전환 (로드 밸런서 설정 변경)
# Green이 정상이면 Blue 종료
docker-compose -f docker-compose.blue.yml down
```

---

## 참고 자료

- [GitHub Actions 공식 문서](https://docs.github.com/actions)
- [Docker 공식 문서](https://docs.docker.com/)
- [AWS EC2 사용자 가이드](https://docs.aws.amazon.com/ec2/)

