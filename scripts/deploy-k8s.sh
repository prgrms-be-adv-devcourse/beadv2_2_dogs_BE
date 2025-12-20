#!/bin/bash

# ===================================
# k8s 배포 스크립트
# Usage: bash deploy-k8s.sh [MODULE_NAME] [IMAGE_TAG]
# Example: bash deploy-k8s.sh auth latest
#          bash deploy-k8s.sh baro-auth main-auth-abc123d
# ===================================

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 로그 함수
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# ===================================
# 파라미터 검증
# ===================================
MODULE_NAME=${1:-}
IMAGE_TAG=${2:-latest}

if [ -z "$MODULE_NAME" ]; then
    log_error "모듈 이름을 지정해주세요."
    echo "Usage: bash deploy-k8s.sh [MODULE_NAME] [IMAGE_TAG]"
    echo ""
    echo "Available modules:"
    echo "  - cloud   (Spring Cloud: Eureka, Gateway, Config)"
    echo "  - auth    (인증 모듈)"
    echo "  - buyer   (구매자 모듈)"
    echo "  - seller  (판매자 모듈)"
    echo "  - order   (주문 모듈)"
    echo "  - support (지원 모듈)"
    echo "  - redis   (Redis 캐시)"
    exit 1
fi

# ===================================
# k8s 디렉토리 자동 탐색
# ===================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_BASE_DIR=""

# 여러 경로에서 k8s 디렉토리 찾기
if [ -d "$SCRIPT_DIR/../k8s/cloud" ]; then
    K8S_BASE_DIR="$SCRIPT_DIR/../k8s"
elif [ -d "$SCRIPT_DIR/../../k8s/cloud" ]; then
    K8S_BASE_DIR="$SCRIPT_DIR/../../k8s"
elif [ -d "/home/ubuntu/apps/BE/k8s/cloud" ]; then
    K8S_BASE_DIR="/home/ubuntu/apps/BE/k8s"
elif [ -d "./k8s/cloud" ]; then
    K8S_BASE_DIR="./k8s"
else
    log_error "k8s 디렉토리를 찾을 수 없습니다."
    exit 1
fi

log_info "k8s 디렉토리: $K8S_BASE_DIR"

# ===================================
# kubectl 확인 및 테스트
# ===================================
log_step "🔍 Checking kubectl..."
KUBECTL_CMD=""

# 1. kubectl이 있는지 확인하고 실제로 동작하는지 테스트
if command -v kubectl &> /dev/null; then
    # kubectl이 실제로 클러스터에 접근할 수 있는지 테스트
    if kubectl get nodes &> /dev/null 2>&1; then
        KUBECTL_CMD="kubectl"
        log_info "✅ 일반 kubectl 사용 가능 (클러스터 접근 성공)"
    elif command -v k3s &> /dev/null; then
        # kubectl이 있지만 클러스터에 접근 실패, sudo k3s kubectl 시도
        if sudo k3s kubectl get nodes &> /dev/null 2>&1; then
            KUBECTL_CMD="sudo k3s kubectl"
            log_info "✅ sudo k3s kubectl 사용 (일반 kubectl은 permission 문제)"
        fi
    fi
fi

# 2. kubectl이 없거나 동작하지 않으면 sudo k3s kubectl 시도
if [ -z "$KUBECTL_CMD" ] && command -v k3s &> /dev/null; then
    if sudo k3s kubectl get nodes &> /dev/null 2>&1; then
        KUBECTL_CMD="sudo k3s kubectl"
        log_info "✅ sudo k3s kubectl 사용"
    fi
fi

# 3. 최종 확인
if [ -z "$KUBECTL_CMD" ]; then
    log_error "kubectl 또는 k3s가 설치되어 있지 않거나 클러스터에 연결할 수 없습니다."
    echo "디버깅 정보:"
    if command -v kubectl &> /dev/null; then
        echo "kubectl get nodes 결과:"
        kubectl get nodes 2>&1 || true
    fi
    if command -v k3s &> /dev/null; then
        echo "sudo k3s kubectl get nodes 결과:"
        sudo k3s kubectl get nodes 2>&1 || true
    fi
    exit 1
fi

log_info "📦 사용할 kubectl 명령어: $KUBECTL_CMD"

# ===================================
# EC2 Private IP 자동 감지 (여러 방법 시도)
# ===================================
log_step "🌐 Detecting EC2 Private IP..."
EC2_IP=""

# 방법 1: EC2 메타데이터 서비스
EC2_IP=$(curl -s --max-time 2 http://169.254.169.254/latest/meta-data/local-ipv4 2>/dev/null || echo "")

# 방법 2: hostname -I 사용
if [ -z "$EC2_IP" ]; then
    EC2_IP=$(hostname -I | awk '{print $1}' 2>/dev/null || echo "")
fi

# 방법 3: ip 명령어 사용
if [ -z "$EC2_IP" ]; then
    EC2_IP=$(ip route get 8.8.8.8 2>/dev/null | awk '{print $7; exit}' || echo "")
fi

if [ -z "$EC2_IP" ]; then
    log_warn "EC2 Private IP를 자동으로 감지할 수 없습니다."
    log_warn "hostNetwork를 사용하므로 127.0.0.1을 사용합니다."
    EC2_IP="127.0.0.1"
fi

log_info "📍 EC2 Private IP: $EC2_IP"

# ===================================
# 모듈별 배포 경로 결정
# ===================================
case "$MODULE_NAME" in
    cloud)
        DEPLOY_PATH=""
        APP_NAME="cloud"
        ;;
    eureka|config|gateway)
        DEPLOY_PATH="$K8S_BASE_DIR/cloud/$MODULE_NAME"
        APP_NAME="$MODULE_NAME"
        ;;
    redis)
        DEPLOY_PATH="$K8S_BASE_DIR/redis"
        APP_NAME="redis"
        ;;
    auth|baro-auth)
        DEPLOY_PATH="$K8S_BASE_DIR/apps/baro-auth"
        APP_NAME="baro-auth"
        ;;
    buyer|baro-buyer)
        DEPLOY_PATH="$K8S_BASE_DIR/apps/baro-buyer"
        APP_NAME="baro-buyer"
        ;;
    seller|baro-seller)
        DEPLOY_PATH="$K8S_BASE_DIR/apps/baro-seller"
        APP_NAME="baro-seller"
        ;;
    order|baro-order)
        DEPLOY_PATH="$K8S_BASE_DIR/apps/baro-order"
        APP_NAME="baro-order"
        ;;
    support|baro-support)
        DEPLOY_PATH="$K8S_BASE_DIR/apps/baro-support"
        APP_NAME="baro-support"
        ;;
    *)
        log_error "알 수 없는 모듈: $MODULE_NAME"
        exit 1
        ;;
esac

# ===================================
# Cloud 모듈 배포 (Eureka → Config → Gateway)
# ===================================
if [ "$MODULE_NAME" = "cloud" ]; then
    log_step "☁️  Cloud 모듈 전체 배포 시작..."
    
    log_step "1️⃣ Eureka 배포 중..."
    $KUBECTL_CMD apply -f "$K8S_BASE_DIR/cloud/eureka/"
    if [ "$IMAGE_TAG" != "latest" ]; then
        $KUBECTL_CMD set image deployment/eureka eureka=ghcr.io/do-develop-space/eureka:${IMAGE_TAG} -n baro-prod || true
    fi
    $KUBECTL_CMD wait --for=condition=ready pod -l app=eureka -n baro-prod --timeout=300s || true
    
    log_step "2️⃣ Config 배포 중..."
    $KUBECTL_CMD apply -f "$K8S_BASE_DIR/cloud/config/"
    if [ "$IMAGE_TAG" != "latest" ]; then
        $KUBECTL_CMD set image deployment/config config=ghcr.io/do-develop-space/config:${IMAGE_TAG} -n baro-prod || true
    fi
    $KUBECTL_CMD wait --for=condition=ready pod -l app=config -n baro-prod --timeout=300s || true
    
    log_step "3️⃣ Gateway 배포 중..."
    $KUBECTL_CMD apply -f "$K8S_BASE_DIR/cloud/gateway/"
    if [ "$IMAGE_TAG" != "latest" ]; then
        $KUBECTL_CMD set image deployment/gateway gateway=ghcr.io/do-develop-space/gateway:${IMAGE_TAG} -n baro-prod || true
    fi
    $KUBECTL_CMD wait --for=condition=ready pod -l app=gateway -n baro-prod --timeout=300s || true
    
    log_info "✅ Cloud 모듈 배포 완료"
    $KUBECTL_CMD get pods -n baro-prod -l component=cloud
    exit 0
fi

# ===================================
# 배포 경로 확인
# ===================================
if [ ! -d "$DEPLOY_PATH" ]; then
    log_error "배포 경로를 찾을 수 없습니다: $DEPLOY_PATH"
    exit 1
fi

# ===================================
# Deployment 파일에 EC2 IP 설정
# ===================================
DEPLOYMENT_FILE="$DEPLOY_PATH/deployment.yaml"
if [ -f "$DEPLOYMENT_FILE" ]; then
    # hostNetwork를 사용하는 경우 EC2 IP 설정이 불필요할 수 있지만,
    # 기존 코드 호환성을 위해 주석 처리 (필요시 활성화)
    # log_step "🔧 EC2 IP 설정 중: $DEPLOYMENT_FILE"
    # sed -i.bak "s/CHANGE_ME_TO_EC2_IP/$EC2_IP/g" "$DEPLOYMENT_FILE"
    # rm -f "${DEPLOYMENT_FILE}.bak" 2>/dev/null || true
    log_info "ℹ️  hostNetwork 사용으로 EC2 IP 설정 불필요 (127.0.0.1 사용)"
fi

# ===================================
# 이미지 태그 업데이트
# ===================================
if [ -f "$DEPLOYMENT_FILE" ] && [ "$IMAGE_TAG" != "latest" ]; then
    log_step "🏷️  이미지 태그 업데이트: $IMAGE_TAG"
    # ghcr.io/do-develop-space/<service>:latest -> ghcr.io/do-develop-space/<service>:$IMAGE_TAG
    SERVICE_NAME=$(grep -E "image:" "$DEPLOYMENT_FILE" | head -1 | sed -E 's/.*image:.*\/([^:]+):.*/\1/')
    if [ -n "$SERVICE_NAME" ]; then
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS
            sed -i '' "s|ghcr.io/do-develop-space/${SERVICE_NAME}:latest|ghcr.io/do-develop-space/${SERVICE_NAME}:${IMAGE_TAG}|g" "$DEPLOYMENT_FILE"
        else
            # Linux
            sed -i "s|ghcr.io/do-develop-space/${SERVICE_NAME}:latest|ghcr.io/do-develop-space/${SERVICE_NAME}:${IMAGE_TAG}|g" "$DEPLOYMENT_FILE"
        fi
        log_info "✅ 이미지 태그 업데이트 완료: ${SERVICE_NAME}:${IMAGE_TAG}"
    fi
fi

# ===================================
# k8s 배포
# ===================================
log_step "📦 k8s 리소스 적용 중..."
$KUBECTL_CMD apply -f "$DEPLOY_PATH/"

# ===================================
# 배포 상태 확인
# ===================================
if [ -f "$DEPLOYMENT_FILE" ]; then
    DEPLOYMENT_NAME=$(grep -E "^  name:" "$DEPLOYMENT_FILE" | head -1 | awk '{print $2}' || echo "")
    if [ -n "$DEPLOYMENT_NAME" ]; then
        log_step "⏳ Pod가 Ready 상태가 될 때까지 대기 중..."
        $KUBECTL_CMD wait --for=condition=ready pod -l app="$APP_NAME" -n baro-prod --timeout=300s || {
            log_warn "Pod가 Ready 상태가 되지 않았습니다. 로그를 확인하세요."
            $KUBECTL_CMD get pods -n baro-prod -l app="$APP_NAME"
            exit 1
        }
        
        log_info "✅ 배포 완료: $MODULE_NAME"
        $KUBECTL_CMD get pods -n baro-prod -l app="$APP_NAME"
    else
        log_info "✅ 리소스 적용 완료: $MODULE_NAME"
    fi
else
    log_info "✅ 리소스 적용 완료: $MODULE_NAME"
fi

log_info "🎉 Deployment completed!"
