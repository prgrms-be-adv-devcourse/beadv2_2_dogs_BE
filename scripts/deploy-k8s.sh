#!/bin/bash

# ===================================
# k8s 배포 스크립트
# Usage: bash deploy-k8s.sh [MODULE_NAME] [IMAGE_TAG]
# Example: bash deploy-k8s.sh auth latest
#          bash deploy-k8s.sh baro-auth main-auth-abc123d
#
# 환경변수:
#   DATA_EC2_IP: Data EC2의 Private IP
#                - Data EC2와 k8s Worker EC2 분리에서는 필수
#                - 설정하지 않으면 현재 EC2(스크립트 실행 EC2)의 IP를 자동 감지
#                  (잘못된 IP가 감지되므로 반드시 설정 필요)
# Example: DATA_EC2_IP=10.0.1.100 bash deploy-k8s.sh auth latest
# ===================================

set -e

# 에러 트랩: 스크립트가 실패한 위치를 로그로 출력 (로그 함수가 정의되기 전에는 사용 불가)
# trap는 나중에 설정됨 (로그 함수 정의 후)

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
    echo "  - cloud      (Spring Cloud: Eureka, Gateway, Config)"
    echo "  - auth       (인증 모듈)"
    echo "  - buyer      (구매자 모듈)"
    echo "  - seller     (판매자 모듈)"
    echo "  - order      (주문 모듈)"
    echo "  - payment    (결제 모듈)"
    echo "  - support    (지원 모듈)"
    echo "  - settlement (정산 모듈, Deployment + CronJob)"
    echo "  - ai         (AI 모듈)"
    echo "  - redis      (Redis 캐시)"
    echo "  - data       (데이터 인프라: MySQL, Kafka, Elasticsearch - docker-compose로 배포)"
    exit 1
fi

# ===================================
# k8s 디렉토리 자동 탐색
# ===================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_BASE_DIR=""

# 여러 경로에서 k8s 디렉토리 찾기 (우선순위 순)
# EC2 배포 시에는 절대 경로 /home/ubuntu/apps/k8s를 우선 사용 (GitHub Actions가 복사하는 경로)
# GitHub Actions runner에서는 워크스페이스의 k8s 디렉토리를 사용
if [ -d "/home/ubuntu/apps/k8s/cloud" ]; then
    # EC2 배포 기준 디렉토리 (GitHub Actions가 복사하는 경로 - 최우선)
    K8S_BASE_DIR="/home/ubuntu/apps/k8s"
elif [ -d "$SCRIPT_DIR/../k8s/cloud" ]; then
    # 스크립트 기준 상대 경로 (GitHub Actions runner에서 가장 가능성 높음)
    K8S_BASE_DIR="$SCRIPT_DIR/../k8s"
elif [ -d "$SCRIPT_DIR/../../k8s/cloud" ]; then
    # 스크립트 기준 상위 상위 경로
    K8S_BASE_DIR="$SCRIPT_DIR/../../k8s"
elif [ -d "./k8s/cloud" ]; then
    # 현재 디렉토리 기준
    K8S_BASE_DIR="./k8s"
elif [ -d "/home/ubuntu/apps/BE/k8s/cloud" ]; then
    # EC2 BE 디렉토리 (fallback)
    K8S_BASE_DIR="/home/ubuntu/apps/BE/k8s"
else
    log_error "k8s 디렉토리를 찾을 수 없습니다."
    log_error "다음 경로를 확인했습니다:"
    log_error "  - /home/ubuntu/apps/k8s"
    log_error "  - $SCRIPT_DIR/../k8s"
    log_error "  - $SCRIPT_DIR/../../k8s"
    log_error "  - ./k8s"
    log_error "  - /home/ubuntu/apps/BE/k8s"
    exit 1
fi

log_info "k8s 디렉토리: $K8S_BASE_DIR"

# ===================================
# kubectl 확인 및 테스트
# ===================================
log_step "🔍 Checking kubectl..."

# kubectl 명령어를 결정하는 함수 (재사용 가능)
# 주의: 이 함수는 stdout에만 명령어를 출력하고, 로그는 출력하지 않음
determine_kubectl_cmd() {
    local cmd=""
    
    # k3s가 설치되어 있으면 우선적으로 sudo k3s kubectl 사용 (권한 문제 방지)
    if command -v k3s &> /dev/null; then
        if sudo k3s kubectl get nodes &> /dev/null 2>&1; then
            cmd="sudo k3s kubectl"
            echo "$cmd"      # stdout으로만 반환 (로그 없음)
            return 0
        fi
    fi
    
    # k3s가 없거나 sudo k3s kubectl이 실패하면 일반 kubectl 시도
    if command -v kubectl &> /dev/null; then
        # kubectl이 실제로 클러스터에 접근할 수 있는지 테스트
        if kubectl get nodes &> /dev/null 2>&1; then
            cmd="kubectl"
            echo "$cmd"      # stdout으로만 반환 (로그 없음)
            return 0
        fi
    fi
    
    return 1
}

# kubectl 명령어 결정
KUBECTL_CMD=$(determine_kubectl_cmd)

# 최종 확인 및 로그 출력
if [ -z "$KUBECTL_CMD" ]; then
    log_error "kubectl 또는 k3s가 설치되어 있지 않거나 클러스터에 연결할 수 없습니다."
    echo "디버깅 정보:"
    if command -v k3s &> /dev/null; then
        echo "sudo k3s kubectl get nodes 결과:"
        sudo k3s kubectl get nodes 2>&1 || true
    fi
    if command -v kubectl &> /dev/null; then
        echo "kubectl get nodes 결과:"
        kubectl get nodes 2>&1 || true
    fi
    exit 1
fi

# kubectl 명령어 타입에 따라 로그 출력
if [[ "$KUBECTL_CMD" == "sudo k3s kubectl" ]]; then
    log_info "✅ sudo k3s kubectl 사용 (k3s 환경 감지)"
elif [[ "$KUBECTL_CMD" == "kubectl" ]]; then
    log_info "✅ 일반 kubectl 사용 가능 (클러스터 접근 성공)"
fi
log_info "📦 사용할 kubectl 명령어: $KUBECTL_CMD"

# kubectl 명령어 검증 함수 (실행 전 확인용)
verify_kubectl() {
    # KUBECTL_CMD가 비어있으면 재확인
    if [ -z "$KUBECTL_CMD" ]; then
        log_warn "⚠️ kubectl 명령어가 설정되지 않았습니다. 재확인 중..."
        KUBECTL_CMD=$(determine_kubectl_cmd)
        if [ -z "$KUBECTL_CMD" ]; then
            log_error "❌ kubectl 명령어를 재확인할 수 없습니다."
            return 1
        fi
        log_info "✅ kubectl 명령어 재확인 완료: $KUBECTL_CMD"
        return 0
    fi
    
    # kubectl 명령어가 작동하는지 확인
    if ! $KUBECTL_CMD get nodes &> /dev/null 2>&1; then
        log_warn "⚠️ kubectl 명령어가 작동하지 않습니다. 재확인 중..."
        KUBECTL_CMD=$(determine_kubectl_cmd)
        if [ -z "$KUBECTL_CMD" ]; then
            log_error "❌ kubectl 명령어를 재확인할 수 없습니다."
            return 1
        fi
        log_info "✅ kubectl 명령어 재확인 완료: $KUBECTL_CMD"
    fi
    return 0
}

# ===================================
# Data EC2 Private IP 설정
# ===================================
# 시나리오 2: Data EC2와 k8s Worker EC2가 분리된 구조
# Data EC2의 Private IP를 환경변수로 받거나, 자동 감지
log_step "🌐 Setting Data EC2 Private IP..."

# DATA_EC2_IP 환경변수가 설정되어 있으면 사용
# 없으면 현재 EC2(스크립트가 실행되는 EC2)의 IP를 자동 감지
# ⚠️ 주의: 시나리오 2(분리된 EC2)에서는 자동 감지된 IP는 k8s Worker EC2의 IP이므로
#          Data EC2 IP와 다를 수 있습니다. 반드시 환경변수로 설정하세요.
DATA_EC2_IP="${DATA_EC2_IP:-}"

if [ -z "$DATA_EC2_IP" ]; then
    log_warn "⚠️  DATA_EC2_IP 환경변수가 설정되지 않았습니다."
    log_warn "⚠️  현재 EC2(스크립트 실행 EC2)의 IP를 자동 감지합니다."
    log_warn "⚠️  시나리오 2(분리된 EC2)에서는 이 IP가 Data EC2 IP와 다를 수 있습니다!"

# 방법 1: EC2 메타데이터 서비스
    DATA_EC2_IP=$(curl -s --max-time 2 http://169.254.169.254/latest/meta-data/local-ipv4 2>/dev/null || echo "")

# 방법 2: hostname -I 사용
    if [ -z "$DATA_EC2_IP" ]; then
        DATA_EC2_IP=$(hostname -I | awk '{print $1}' 2>/dev/null || echo "")
fi

# 방법 3: ip 명령어 사용
    if [ -z "$DATA_EC2_IP" ]; then
        DATA_EC2_IP=$(ip route get 8.8.8.8 2>/dev/null | awk '{print $7; exit}' || echo "")
fi

    if [ -z "$DATA_EC2_IP" ]; then
        log_error "❌ IP를 자동으로 감지할 수 없습니다."
        log_error "환경변수 DATA_EC2_IP를 설정하거나, EC2 메타데이터 서비스에 접근할 수 있는지 확인하세요."
        exit 1
    fi
    log_info "✅ 현재 EC2 IP 자동 감지: $DATA_EC2_IP (시나리오 2에서는 Data EC2 IP와 다를 수 있음)"
else
    log_info "✅ 환경변수 DATA_EC2_IP 사용: $DATA_EC2_IP"
fi

log_info "📍 Data EC2 Private IP: $DATA_EC2_IP"
log_info "💡 다른 EC2의 Data 서비스(MySQL, Kafka, Redis, ES)에 접근할 때 이 IP를 사용합니다."

# ===================================
# Namespace 생성 (base kustomization 사용)
# ===================================
log_step "📦 Applying base resources (namespace)..."
verify_kubectl || exit 1
if $KUBECTL_CMD apply -k "$K8S_BASE_DIR/base/"; then
    log_info "✅ Base resources (namespace) 적용 완료"
else
    log_error "❌ Base resources 적용 실패"
    exit 1
fi

# ===================================
# 모듈별 배포 경로 결정
# ===================================
case "$MODULE_NAME" in
    cloud)
        DEPLOY_PATH=""
        APP_NAME="cloud"
        ;;
    eureka|config|gateway|opa)
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
    payment|baro-payment)
        DEPLOY_PATH="$K8S_BASE_DIR/apps/baro-payment"
        APP_NAME="baro-payment"
        ;;
    support|baro-support)
        DEPLOY_PATH="$K8S_BASE_DIR/apps/baro-support"
        APP_NAME="baro-support"
        ;;
    settlement|baro-settlement)
        DEPLOY_PATH="$K8S_BASE_DIR/apps/baro-settlement"
        APP_NAME="baro-settlement"
        ;;
    ai|baro-ai)
        DEPLOY_PATH="$K8S_BASE_DIR/apps/baro-ai"
        APP_NAME="baro-ai"
        ;;
    data)
        # data 모듈은 docker-compose로 배포
        log_step "📦 'data' 모듈 배포 (docker-compose 사용)"
        log_info "💡 'data' 모듈(MySQL, Kafka, Elasticsearch)은 docker-compose로 배포됩니다."
        
        # deploy-module.sh 스크립트 찾기
        DEPLOY_MODULE_SCRIPT=""
        if [ -f "$SCRIPT_DIR/deploy-module.sh" ]; then
            DEPLOY_MODULE_SCRIPT="$SCRIPT_DIR/deploy-module.sh"
        elif [ -f "/home/ubuntu/apps/BE/deploy-module.sh" ]; then
            DEPLOY_MODULE_SCRIPT="/home/ubuntu/apps/BE/deploy-module.sh"
        elif [ -f "./scripts/deploy-module.sh" ]; then
            DEPLOY_MODULE_SCRIPT="./scripts/deploy-module.sh"
        else
            log_error "deploy-module.sh 스크립트를 찾을 수 없습니다."
            exit 1
        fi
        
        log_info "🚀 docker-compose 배포 시작..."
        bash "$DEPLOY_MODULE_SCRIPT" data
        log_info "✅ 'data' 모듈 배포 완료"
        exit 0
        ;;
    *)
        log_error "알 수 없는 모듈: $MODULE_NAME"
        log_info "사용 가능한 모듈: cloud, eureka, config, gateway, redis, auth, buyer, seller, order, payment, support, settlement, ai, data"
        exit 1
        ;;
esac

# ===================================
# Cloud 모듈 배포 (Eureka → Config → Gateway)
# ===================================
if [ "$MODULE_NAME" = "cloud" ]; then
    log_step "☁️  Cloud 모듈 전체 배포 시작..."
    
    log_step "1️⃣ Eureka 배포 중..."
    # kustomization.yaml에서 이미지 태그 업데이트
    KUSTOMIZATION_FILE="$K8S_BASE_DIR/cloud/eureka/kustomization.yaml"
    if [ -f "$KUSTOMIZATION_FILE" ] && [ "$IMAGE_TAG" != "latest" ]; then
        log_info "🏷️  Eureka 이미지 태그 업데이트: $IMAGE_TAG"
        sed -i.bak "s|newTag: latest|newTag: ${IMAGE_TAG}|g" "$KUSTOMIZATION_FILE" 2>/dev/null || \
        sed -i "s|newTag: latest|newTag: ${IMAGE_TAG}|g" "$KUSTOMIZATION_FILE" 2>/dev/null || true
        rm -f "${KUSTOMIZATION_FILE}.bak" 2>/dev/null || true
    fi
    $KUBECTL_CMD apply -k "$K8S_BASE_DIR/cloud/eureka/"
    
    # IMAGE_TAG가 latest일 때는 Deployment spec이 변경되지 않으므로 rollout restart로 Pod 재시작
    if [ "$IMAGE_TAG" = "latest" ]; then
        log_info "🔄 latest 태그 사용 중이므로 Pod 재시작 (rollout restart)..."
        $KUBECTL_CMD rollout restart deployment/eureka -n baro-prod || true
    fi
    
    # Pod가 Ready 상태가 될 때까지 대기 (타임아웃: 300초)
    if ! $KUBECTL_CMD wait --for=condition=ready pod -l app=eureka -n baro-prod --timeout=300s 2>&1; then
        log_warn "⚠️ Eureka Pod가 Ready 상태가 되지 않았습니다. 상태 확인 중..."
        echo ""
        echo "📊 Eureka Pod 상태:"
        $KUBECTL_CMD get pods -n baro-prod -l app=eureka 2>&1 || true
        echo ""
        echo "📋 Eureka Deployment 상태:"
        $KUBECTL_CMD get deployment eureka -n baro-prod 2>&1 || true
        echo ""
        echo "📝 Eureka Pod 이벤트:"
        EUREKA_POD=$($KUBECTL_CMD get pods -n baro-prod -l app=eureka -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
        if [ -n "$EUREKA_POD" ]; then
            $KUBECTL_CMD describe pod "$EUREKA_POD" -n baro-prod 2>&1 | grep -A 30 "Events:" || true
            echo ""
            echo "📄 Eureka Pod 로그 (마지막 50줄):"
            $KUBECTL_CMD logs "$EUREKA_POD" -n baro-prod --tail=50 2>&1 || true
        fi
        log_warn "⚠️ Eureka 배포는 계속 진행하지만, Pod가 준비되지 않았을 수 있습니다."
    else
        log_info "✅ Eureka Pod가 Ready 상태입니다."
    fi
    
    # 2️⃣ Config 배포 (주석 처리)
    # log_step "2️⃣ Config 배포 중..."
    # KUSTOMIZATION_FILE="$K8S_BASE_DIR/cloud/config/kustomization.yaml"
    # if [ -f "$KUSTOMIZATION_FILE" ] && [ "$IMAGE_TAG" != "latest" ]; then
    #     log_info "🏷️  Config 이미지 태그 업데이트: $IMAGE_TAG"
    #     sed -i.bak "s|newTag: latest|newTag: ${IMAGE_TAG}|g" "$KUSTOMIZATION_FILE" 2>/dev/null || \
    #     sed -i "s|newTag: latest|newTag: ${IMAGE_TAG}|g" "$KUSTOMIZATION_FILE" 2>/dev/null || true
    #     rm -f "${KUSTOMIZATION_FILE}.bak" 2>/dev/null || true
    # fi
    # $KUBECTL_CMD apply -k "$K8S_BASE_DIR/cloud/config/"
    # if [ "$IMAGE_TAG" = "latest" ]; then
    #     log_info "🔄 latest 태그 사용 중이므로 Pod 재시작 (rollout restart)..."
    #     $KUBECTL_CMD rollout restart deployment/config -n baro-prod || true
    # fi
    # if ! $KUBECTL_CMD wait --for=condition=ready pod -l app=config -n baro-prod --timeout=300s 2>&1; then
    #     log_warn "⚠️ Config Pod가 Ready 상태가 되지 않았습니다. 상태 확인 중..."
    #     echo ""
    #     echo "📊 Config Pod 상태:"
    #     $KUBECTL_CMD get pods -n baro-prod -l app=config 2>&1 || true
    #     echo ""
    #     echo "📋 Config Deployment 상태:"
    #     $KUBECTL_CMD get deployment config -n baro-prod 2>&1 || true
    #     echo ""
    #     echo "📝 Config Pod 이벤트:"
    #     CONFIG_POD=$($KUBECTL_CMD get pods -n baro-prod -l app=config -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
    #     if [ -n "$CONFIG_POD" ]; then
    #         $KUBECTL_CMD describe pod "$CONFIG_POD" -n baro-prod 2>&1 | grep -A 30 "Events:" || true
    #         echo ""
    #         echo "📄 Config Pod 로그 (마지막 50줄):"
    #         $KUBECTL_CMD logs "$CONFIG_POD" -n baro-prod --tail=50 2>&1 || true
    #     fi
    #     log_warn "⚠️ Config 배포는 계속 진행하지만, Pod가 준비되지 않았을 수 있습니다."
    # else
    #     log_info "✅ Config Pod가 Ready 상태입니다."
    # fi
    
    log_step "3️⃣ Gateway 배포 중..."
    # kustomization.yaml에서 이미지 태그 업데이트
    KUSTOMIZATION_FILE="$K8S_BASE_DIR/cloud/gateway/kustomization.yaml"
    if [ -f "$KUSTOMIZATION_FILE" ] && [ "$IMAGE_TAG" != "latest" ]; then
        log_info "🏷️  Gateway 이미지 태그 업데이트: $IMAGE_TAG"
        sed -i.bak "s|newTag: latest|newTag: ${IMAGE_TAG}|g" "$KUSTOMIZATION_FILE" 2>/dev/null || \
        sed -i "s|newTag: latest|newTag: ${IMAGE_TAG}|g" "$KUSTOMIZATION_FILE" 2>/dev/null || true
        rm -f "${KUSTOMIZATION_FILE}.bak" 2>/dev/null || true
    fi
    $KUBECTL_CMD apply -k "$K8S_BASE_DIR/cloud/gateway/"
    
    # IMAGE_TAG가 latest일 때는 Deployment spec이 변경되지 않으므로 rollout restart로 Pod 재시작
    if [ "$IMAGE_TAG" = "latest" ]; then
        log_info "🔄 latest 태그 사용 중이므로 Pod 재시작 (rollout restart)..."
        $KUBECTL_CMD rollout restart deployment/gateway -n baro-prod || true
    fi
    
    # Pod가 Ready 상태가 될 때까지 대기 (타임아웃: 300초)
    if ! $KUBECTL_CMD wait --for=condition=ready pod -l app=gateway -n baro-prod --timeout=300s 2>&1; then
        log_warn "⚠️ Gateway Pod가 Ready 상태가 되지 않았습니다. 상태 확인 중..."
        echo ""
        echo "📊 Gateway Pod 상태:"
        $KUBECTL_CMD get pods -n baro-prod -l app=gateway 2>&1 || true
        echo ""
        echo "📋 Gateway Deployment 상태:"
        $KUBECTL_CMD get deployment gateway -n baro-prod 2>&1 || true
        echo ""
        echo "📝 Gateway Pod 이벤트:"
        GATEWAY_POD=$($KUBECTL_CMD get pods -n baro-prod -l app=gateway -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
        if [ -n "$GATEWAY_POD" ]; then
            $KUBECTL_CMD describe pod "$GATEWAY_POD" -n baro-prod 2>&1 | grep -A 30 "Events:" || true
            echo ""
            echo "📄 Gateway Pod 로그 (마지막 50줄):"
            $KUBECTL_CMD logs "$GATEWAY_POD" -n baro-prod --tail=50 2>&1 || true
        fi
        log_warn "⚠️ Gateway 배포는 계속 진행하지만, Pod가 준비되지 않았을 수 있습니다."
    else
        log_info "✅ Gateway Pod가 Ready 상태입니다."
    fi
    
    log_step "4️⃣ OPA 배포 중..."
    # kustomization.yaml에서 이미지 태그 업데이트
    KUSTOMIZATION_FILE="$K8S_BASE_DIR/cloud/opa/kustomization.yaml"
    if [ -f "$KUSTOMIZATION_FILE" ] && [ "$IMAGE_TAG" != "latest" ]; then
        log_info "🏷️  OPA 이미지 태그 업데이트: $IMAGE_TAG"
        sed -i.bak "s|newTag: latest|newTag: ${IMAGE_TAG}|g" "$KUSTOMIZATION_FILE" 2>/dev/null || \
        sed -i "s|newTag: latest|newTag: ${IMAGE_TAG}|g" "$KUSTOMIZATION_FILE" 2>/dev/null || true
        rm -f "${KUSTOMIZATION_FILE}.bak" 2>/dev/null || true
    fi
    
    # OPA Bundle 배포 전 Eureka 확인
    log_step "⏳ OPA Bundle 배포 전 Eureka 준비 상태 확인 중..."
    if ! $KUBECTL_CMD wait --for=condition=ready pod -l app=eureka -n baro-prod --timeout=60s 2>&1; then
        log_warn "⚠️ Eureka Pod가 Ready 상태가 아닙니다. OPA Bundle 배포를 계속 진행하지만, initContainer에서 대기합니다."
    else
        log_info "✅ Eureka Pod가 Ready 상태입니다."
    fi
    
    # OPA Bundle 배포 (Deployment)
    log_step "📦 OPA Bundle 배포 중..."
    $KUBECTL_CMD apply -k "$K8S_BASE_DIR/cloud/opa-bundle/"
    
    # IMAGE_TAG가 latest일 때는 Deployment spec이 변경되지 않으므로 rollout restart로 Pod 재시작
    if [ "$IMAGE_TAG" = "latest" ]; then
        log_info "🔄 latest 태그 사용 중이므로 Pod 재시작 (rollout restart)..."
        verify_kubectl || exit 1
        $KUBECTL_CMD rollout restart deployment/opa-bundle -n baro-prod || true
    fi
    
    # Pod가 Ready 상태가 될 때까지 대기 (타임아웃: 300초)
    verify_kubectl || exit 1
    if ! $KUBECTL_CMD wait --for=condition=ready pod -l app=opa-bundle -n baro-prod --timeout=300s 2>&1; then
        log_warn "⚠️ OPA Bundle Pod가 Ready 상태가 되지 않았습니다. 상태 확인 중..."
        echo ""
        echo "📊 OPA Bundle Pod 상태:"
        $KUBECTL_CMD get pods -n baro-prod -l app=opa-bundle 2>&1 || true
        echo ""
        echo "📋 OPA Bundle Deployment 상태:"
        $KUBECTL_CMD get deployment opa-bundle -n baro-prod 2>&1 || true
        echo ""
        echo "📝 OPA Bundle Pod 이벤트:"
        OPA_BUNDLE_POD=$($KUBECTL_CMD get pods -n baro-prod -l app=opa-bundle -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
        if [ -n "$OPA_BUNDLE_POD" ]; then
            $KUBECTL_CMD describe pod "$OPA_BUNDLE_POD" -n baro-prod 2>&1 | grep -A 30 "Events:" || true
            echo ""
            echo "📄 OPA Bundle Pod 로그 (마지막 50줄):"
            $KUBECTL_CMD logs "$OPA_BUNDLE_POD" -n baro-prod --tail=50 2>&1 || true
        fi
        log_warn "⚠️ OPA Bundle 배포는 계속 진행하지만, Pod가 준비되지 않았을 수 있습니다."
    else
        log_info "✅ OPA Bundle Pod가 Ready 상태입니다."
    fi
    
    # OPA 배포 전 OPA Bundle Ready 상태 재확인
    log_step "⏳ OPA 배포 전 OPA Bundle Ready 상태 확인 중..."
    if ! $KUBECTL_CMD wait --for=condition=ready pod -l app=opa-bundle -n baro-prod --timeout=30s 2>&1; then
        log_warn "⚠️ OPA Bundle Pod가 Ready 상태가 아닙니다. OPA 배포를 계속 진행하지만, OPA가 번들을 가져올 수 없을 수 있습니다."
    else
        log_info "✅ OPA Bundle Pod가 Ready 상태입니다. OPA 배포를 진행합니다."
    fi
    
    # OPA 배포 (DaemonSet)
    log_step "📦 OPA 배포 중..."
    $KUBECTL_CMD apply -k "$K8S_BASE_DIR/cloud/opa/"
    
    # IMAGE_TAG가 latest일 때는 DaemonSet spec이 변경되지 않으므로 rollout restart로 Pod 재시작
    if [ "$IMAGE_TAG" = "latest" ]; then
        log_info "🔄 latest 태그 사용 중이므로 Pod 재시작 (rollout restart)..."
        verify_kubectl || exit 1
        $KUBECTL_CMD rollout restart daemonset/opa -n baro-prod || true
    fi
    
    # Pod가 Ready 상태가 될 때까지 대기 (타임아웃: 300초)
    verify_kubectl || exit 1
    if ! $KUBECTL_CMD wait --for=condition=ready pod -l app=opa -n baro-prod --timeout=300s 2>&1; then
        log_warn "⚠️ OPA Pod가 Ready 상태가 되지 않았습니다. 상태 확인 중..."
        echo ""
        echo "📊 OPA Pod 상태:"
        $KUBECTL_CMD get pods -n baro-prod -l app=opa 2>&1 || true
        echo ""
        echo "📋 OPA DaemonSet 상태:"
        $KUBECTL_CMD get daemonset opa -n baro-prod 2>&1 || true
        echo ""
        echo "📝 OPA Pod 이벤트:"
        OPA_POD=$($KUBECTL_CMD get pods -n baro-prod -l app=opa -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
        if [ -n "$OPA_POD" ]; then
            $KUBECTL_CMD describe pod "$OPA_POD" -n baro-prod 2>&1 | grep -A 30 "Events:" || true
            echo ""
            echo "📄 OPA Pod 로그 (마지막 50줄):"
            $KUBECTL_CMD logs "$OPA_POD" -n baro-prod --tail=50 2>&1 || true
        fi
        log_warn "⚠️ OPA 배포는 계속 진행하지만, Pod가 준비되지 않았을 수 있습니다."
    else
        log_info "✅ OPA Pod가 Ready 상태입니다."
    fi
    
    log_info "✅ Cloud 모듈 배포 완료"
    $KUBECTL_CMD get pods -n baro-prod -l component=cloud
    exit 0
fi

# ===================================
# 배포 경로 확인
# ===================================
if [ ! -d "$DEPLOY_PATH" ]; then
    log_error "배포 경로를 찾을 수 없습니다: $DEPLOY_PATH"
    log_error "디버깅 정보:"
    log_error "  - K8S_BASE_DIR: $K8S_BASE_DIR"
    log_error "  - MODULE_NAME: $MODULE_NAME"
    log_error "  - 예상 경로: $K8S_BASE_DIR/apps/baro-$MODULE_NAME 또는 $K8S_BASE_DIR/cloud/$MODULE_NAME"
    
    # 가능한 경로 확인
    log_error "가능한 경로 확인:"
    if [ -d "$K8S_BASE_DIR/apps" ]; then
        log_error "  - apps 디렉토리 내용:"
        ls -la "$K8S_BASE_DIR/apps" 2>&1 | head -10 || true
    fi
    if [ -d "$K8S_BASE_DIR/cloud" ]; then
        log_error "  - cloud 디렉토리 내용:"
        ls -la "$K8S_BASE_DIR/cloud" 2>&1 | head -10 || true
    fi
    exit 1
fi

# 배포 경로에 deployment.yaml 또는 daemonset.yaml이 있는지 확인
if [ ! -f "$DEPLOY_PATH/deployment.yaml" ] && [ ! -f "$DEPLOY_PATH/daemonset.yaml" ]; then
    log_error "배포 파일을 찾을 수 없습니다: $DEPLOY_PATH"
    log_error "다음 파일 중 하나가 필요합니다:"
    log_error "  - $DEPLOY_PATH/deployment.yaml"
    log_error "  - $DEPLOY_PATH/daemonset.yaml"
    log_error "디렉토리 내용:"
    ls -la "$DEPLOY_PATH" 2>&1 || true
    exit 1
fi

# ===================================
# kustomization.yaml에서 이미지 태그 업데이트
# ===================================
KUSTOMIZATION_FILE="$DEPLOY_PATH/kustomization.yaml"
if [ -f "$KUSTOMIZATION_FILE" ] && [ "$IMAGE_TAG" != "latest" ]; then
    log_step "🏷️  이미지 태그 업데이트: $IMAGE_TAG"
    # kustomization.yaml에서 이미지 태그 업데이트 (백업 파일 생성)
    if sed -i.bak "s|newTag: latest|newTag: ${IMAGE_TAG}|g" "$KUSTOMIZATION_FILE" 2>/dev/null || \
       sed -i "s|newTag: latest|newTag: ${IMAGE_TAG}|g" "$KUSTOMIZATION_FILE" 2>/dev/null; then
        # 백업 파일은 배포 후 정리 (또는 보존)
        # rm -f "${KUSTOMIZATION_FILE}.bak" 2>/dev/null || true
        log_info "✅ kustomization.yaml 이미지 태그 업데이트 완료 (백업 파일: ${KUSTOMIZATION_FILE}.bak)"
    else
        log_warn "⚠️  kustomization.yaml 이미지 태그 업데이트 실패, 기존 설정 사용"
    fi
fi

# ===================================
# Deployment/DaemonSet 파일에 EC2 IP 설정 (임시 파일 사용)
# ===================================
DEPLOYMENT_FILE="$DEPLOY_PATH/deployment.yaml"
DAEMONSET_FILE="$DEPLOY_PATH/daemonset.yaml"
TEMP_DEPLOYMENT=""

# Deployment 또는 DaemonSet 파일 확인
if [ -f "$DEPLOYMENT_FILE" ]; then
    DEPLOYMENT_FILE_TO_USE="$DEPLOYMENT_FILE"
    log_info "🔍 Deployment 파일 확인: $DEPLOYMENT_FILE"
elif [ -f "$DAEMONSET_FILE" ]; then
    DEPLOYMENT_FILE_TO_USE="$DAEMONSET_FILE"
    log_info "🔍 DaemonSet 파일 확인: $DAEMONSET_FILE"
else
    log_error "❌ Deployment 또는 DaemonSet 파일을 찾을 수 없습니다"
    log_error "디버깅 정보:"
    log_error "  - DEPLOY_PATH: $DEPLOY_PATH"
    log_error "  - K8S_BASE_DIR: $K8S_BASE_DIR"
    log_error "  - MODULE_NAME: $MODULE_NAME"
    
    # 디렉토리 내용 확인
    if [ -d "$DEPLOY_PATH" ]; then
        log_error "  - 디렉토리 내용:"
        ls -la "$DEPLOY_PATH" 2>&1 | head -20 || true
    else
        log_error "  - 디렉토리도 존재하지 않습니다: $DEPLOY_PATH"
    fi
    exit 1
fi

if [ -f "$DEPLOYMENT_FILE_TO_USE" ]; then
    log_info "✅ 파일 존재 확인됨: $DEPLOYMENT_FILE_TO_USE"
    # 임시 파일 생성 (원본 파일 보존)
    TEMP_DEPLOYMENT=$(mktemp)
    cp "$DEPLOYMENT_FILE_TO_USE" "$TEMP_DEPLOYMENT"
    
    log_step "🔧 Deployment 파일 설정 중..."
    
    # hostNetwork 여부 확인 (정보 목적)
    USE_HOST_NETWORK=false
    if grep -q "hostNetwork: true" "$TEMP_DEPLOYMENT"; then
        USE_HOST_NETWORK=true
        log_info "🌐 hostNetwork: true 감지"
        log_info "💡 hostNetwork는 k8s 서비스 접근용이며, Data 서비스는 Data EC2 IP를 사용합니다."
    else
        log_info "🌐 hostNetwork: false"
    fi
    
    # Data EC2 IP 설정 (hostNetwork 여부와 무관하게 Data EC2 IP 사용)
    # 시나리오 2: Data EC2와 k8s Worker EC2가 분리되어 있으므로 항상 Data EC2 IP 사용
    
    if grep -q "CHANGE_ME_TO_EC2_IP" "$TEMP_DEPLOYMENT"; then
        REPLACEMENT_IP="$DATA_EC2_IP"
        log_info "Data EC2 IP 설정 중 (CHANGE_ME_TO_EC2_IP -> $DATA_EC2_IP)"
        
        # 모든 CHANGE_ME_TO_EC2_IP를 치환 IP로 변경 (전역 치환)
        if sed -i.bak "s/CHANGE_ME_TO_EC2_IP/$REPLACEMENT_IP/g" "$TEMP_DEPLOYMENT" 2>/dev/null; then
            rm -f "${TEMP_DEPLOYMENT}.bak" 2>/dev/null || true
        else
            # sed -i가 실패하면 임시 파일 방식 사용
            sed "s/CHANGE_ME_TO_EC2_IP/$REPLACEMENT_IP/g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
            mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
        fi
        
        # 치환 검증
        if grep -q "CHANGE_ME_TO_EC2_IP" "$TEMP_DEPLOYMENT"; then
            log_error "❌ IP 치환이 완료되지 않았습니다. 남은 CHANGE_ME_TO_EC2_IP 패턴:"
            grep -n "CHANGE_ME_TO_EC2_IP" "$TEMP_DEPLOYMENT" || true
            log_error "배포를 중단합니다. 스크립트를 확인하세요."
            rm -f "$TEMP_DEPLOYMENT"
            exit 1
        else
            log_info "✅ IP 치환 완료: $REPLACEMENT_IP"
        fi
    else
        log_info "ℹ️  CHANGE_ME_TO_EC2_IP 패턴이 없습니다. (이미 치환되었거나 불필요)"
    fi
    
    # EC2_IP 환경 변수 설정 (Data EC2 IP 사용)
    if grep -q "name: EC2_IP" "$TEMP_DEPLOYMENT"; then
        if grep -q 'value: "CHANGE_ME_TO_EC2_IP"' "$TEMP_DEPLOYMENT" || grep -q 'value: "127.0.0.1"' "$TEMP_DEPLOYMENT" || grep -q "value: \"" "$TEMP_DEPLOYMENT"; then
            log_info "EC2_IP 환경 변수 설정 중 (Data EC2 IP: $DATA_EC2_IP)"
            sed "/name: EC2_IP/,/value:/ s|value: \".*\"|value: \"$DATA_EC2_IP\"|" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
                mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
        fi
    fi
    
    # SPRING_DATASOURCE_URL에서 $(EC2_IP) 패턴 처리 (Data EC2 IP 사용)
    if grep -q "SPRING_DATASOURCE_URL" "$TEMP_DEPLOYMENT"; then
        log_info "SPRING_DATASOURCE_URL 업데이트 중 (Data EC2 IP: $DATA_EC2_IP)"
        # $(EC2_IP) 패턴을 Data EC2 IP로 변경
        sed "s|\$(EC2_IP)|$DATA_EC2_IP|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
        mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
        
        # 127.0.0.1:3306 패턴도 Data EC2 IP로 변경 (Data EC2가 분리되어 있으므로)
        sed "s|127\.0\.0\.1:3306|$DATA_EC2_IP:3306|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
        mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
        log_info "SPRING_DATASOURCE_URL: Data EC2 IP($DATA_EC2_IP) 사용"
    fi
    
    # Cloud 서비스 접근 URL 처리 (애플리케이션 모듈만, Cloud 모듈은 제외)
    # k8s Worker EC2에서 실행되는 애플리케이션 모듈이 Data EC2의 Cloud 서비스에 접근
    # Cloud 모듈(eureka, config, gateway)은 Data EC2에서 실행되므로 localhost 유지
    if [[ "$DEPLOY_PATH" == *"/apps/"* ]]; then
        log_info "애플리케이션 모듈 감지: Cloud 서비스 접근 URL을 Data EC2 IP로 변경"
        
        # SPRING_CONFIG_IMPORT: localhost:8888 → Data EC2 IP:8888
        # YAML에서 name과 value가 다른 줄에 있으므로, 파일 전체에서 localhost:8888 패턴 검색
        if grep -q "localhost:8888" "$TEMP_DEPLOYMENT"; then
            sed "s|http://localhost:8888|http://$DATA_EC2_IP:8888|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
            mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
            log_info "✅ SPRING_CONFIG_IMPORT: localhost:8888 → $DATA_EC2_IP:8888"
        fi
        
        # EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: localhost:8761 → Data EC2 IP:8761
        # YAML에서 name과 value가 다른 줄에 있으므로, 파일 전체에서 localhost:8761 패턴 검색
        if grep -q "localhost:8761" "$TEMP_DEPLOYMENT"; then
            sed "s|http://localhost:8761/eureka/|http://$DATA_EC2_IP:8761/eureka/|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
            mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
            log_info "✅ EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: localhost:8761 → $DATA_EC2_IP:8761"
        fi
        
        # Kafka Bootstrap Servers 처리
        # Kafka가 Public EC2에서 실행 중이므로 모든 모듈이 Public EC2 IP 사용
        # DEPLOY_KAFKA=true일 때: Kafka는 Public EC2에 배포되므로 Public EC2 IP 사용
        # DEPLOY_KAFKA=false일 때: Kafka가 Public EC2에 있다면 Public EC2 IP 사용, Private EC2에 있다면 localhost 사용
        if grep -q "SPRING_KAFKA_BOOTSTRAP_SERVERS" "$TEMP_DEPLOYMENT"; then
            log_info "🔍 Kafka Bootstrap Servers 치환 전 확인:"
            grep -A 1 "SPRING_KAFKA_BOOTSTRAP_SERVERS" "$TEMP_DEPLOYMENT" || true
            
            # Kafka 위치 확인: Public EC2에서 실행 중이므로 항상 Public EC2 IP 사용
            # DATA_EC2_IP는 Public EC2 IP를 의미 (Kafka가 실행되는 EC2)
            KAFKA_IP="$DATA_EC2_IP"
            log_info "📦 Kafka는 Public EC2($KAFKA_IP)에서 실행 중이므로 모든 모듈이 $KAFKA_IP:29092 사용"
            
            # 모든 모듈(Public/Private EC2 모두)이 Public EC2 IP 사용
            # 127.0.0.1:29092 패턴을 Public EC2 IP로 변경
            sed "s|127\.0\.0\.1:29092|$KAFKA_IP:29092|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
            mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
            # localhost:29092 패턴도 Public EC2 IP로 변경
            sed "s|localhost:29092|$KAFKA_IP:29092|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
            mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
            # http://localhost:29092 패턴도 처리
            sed "s|http://localhost:29092|$KAFKA_IP:29092|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
            mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
            # CHANGE_ME_TO_EC2_IP:29092는 이미 전역 치환으로 Public EC2 IP:29092로 변경됨
            if grep -q "$KAFKA_IP:29092" "$TEMP_DEPLOYMENT"; then
                log_info "✅ SPRING_KAFKA_BOOTSTRAP_SERVERS: $KAFKA_IP:29092 사용 (Kafka는 Public EC2에서 실행 중)"
            fi
            
            log_info "🔍 Kafka Bootstrap Servers 치환 후 확인:"
            grep -A 1 "SPRING_KAFKA_BOOTSTRAP_SERVERS" "$TEMP_DEPLOYMENT" || true
        fi
        
        # Elasticsearch URI 처리
        # DEPLOY_ELASTICSEARCH=true일 때: Public EC2에 배포되므로 DATA_EC2_IP 사용
        # DEPLOY_ELASTICSEARCH=false일 때: Private EC2에 배포되거나 미배포이므로 localhost 사용
        if grep -q "SPRING_ELASTICSEARCH_URIS" "$TEMP_DEPLOYMENT"; then
            if [ "${DEPLOY_ELASTICSEARCH:-false}" = "true" ]; then
                log_info "📦 DEPLOY_ELASTICSEARCH=true: Elasticsearch는 Public EC2에 배포되므로 $DATA_EC2_IP:9200 사용"
                # 127.0.0.1:9200 패턴을 Data EC2 IP로 변경
                sed "s|127\.0\.0\.1:9200|$DATA_EC2_IP:9200|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
                mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
                # localhost:9200 패턴도 Data EC2 IP로 변경
                sed "s|localhost:9200|$DATA_EC2_IP:9200|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
                mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
                # http://localhost:9200 패턴도 처리
                sed "s|http://localhost:9200|http://$DATA_EC2_IP:9200|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
                mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
                # CHANGE_ME_TO_EC2_IP:9200는 이미 전역 치환으로 DATA_EC2_IP:9200로 변경됨
                if grep -q "$DATA_EC2_IP:9200" "$TEMP_DEPLOYMENT"; then
                    log_info "✅ SPRING_ELASTICSEARCH_URIS: $DATA_EC2_IP:9200 사용 (Public EC2에서 실행 중)"
                fi
            else
                log_info "📦 DEPLOY_ELASTICSEARCH=false: Elasticsearch는 Private EC2에 배포되거나 미배포이므로 localhost:9200 유지"
                # CHANGE_ME_TO_EC2_IP:9200를 localhost:9200로 변경
                sed "s|$DATA_EC2_IP:9200|localhost:9200|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
                mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
                # http://localhost:9200 패턴도 유지
                sed "s|http://$DATA_EC2_IP:9200|http://localhost:9200|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
                mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
                # localhost:9200가 이미 설정되어 있으면 유지
                if grep -q "localhost:9200\|127\.0\.0\.1:9200" "$TEMP_DEPLOYMENT"; then
                    log_info "✅ SPRING_ELASTICSEARCH_URIS: localhost:9200 사용 (Private EC2에서 실행 중 또는 미배포)"
                fi
            fi
        fi
    fi
    
    # 최종 검증: CHANGE_ME_TO_EC2_IP가 남아있는지 확인
    if grep -q "CHANGE_ME_TO_EC2_IP" "$TEMP_DEPLOYMENT"; then
        log_error "❌ 치환되지 않은 CHANGE_ME_TO_EC2_IP가 남아있습니다!"
        log_error "다음 위치에서 발견:"
        grep -n "CHANGE_ME_TO_EC2_IP" "$TEMP_DEPLOYMENT" || true
        log_error "배포를 중단합니다. 스크립트를 확인하세요."
        rm -f "$TEMP_DEPLOYMENT"
        exit 1
    fi
    
    # 최종 검증: 127.0.0.1이나 localhost가 Kafka/Elasticsearch에 남아있는지 확인
    if [[ "$DEPLOY_PATH" == *"/apps/"* ]]; then
        if grep -q "SPRING_KAFKA_BOOTSTRAP_SERVERS" "$TEMP_DEPLOYMENT"; then
            if grep -q "SPRING_KAFKA_BOOTSTRAP_SERVERS" "$TEMP_DEPLOYMENT" && (grep -q "127\.0\.0\.1:29092\|localhost:29092" "$TEMP_DEPLOYMENT"); then
                log_warn "⚠️  SPRING_KAFKA_BOOTSTRAP_SERVERS에 127.0.0.1 또는 localhost가 남아있습니다!"
                log_warn "강제로 $DATA_EC2_IP:29092로 변경합니다."
                sed -i.bak "s|value: \".*127\.0\.0\.1:29092.*\"|value: \"$DATA_EC2_IP:29092\"|g" "$TEMP_DEPLOYMENT" 2>/dev/null || \
                sed "s|value: \".*127\.0\.0\.1:29092.*\"|value: \"$DATA_EC2_IP:29092\"|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp" && mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
                sed -i.bak "s|value: \".*localhost:29092.*\"|value: \"$DATA_EC2_IP:29092\"|g" "$TEMP_DEPLOYMENT" 2>/dev/null || \
                sed "s|value: \".*localhost:29092.*\"|value: \"$DATA_EC2_IP:29092\"|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp" && mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
                rm -f "${TEMP_DEPLOYMENT}.bak" 2>/dev/null || true
            fi
        fi
        
        if grep -q "SPRING_ELASTICSEARCH_URIS" "$TEMP_DEPLOYMENT"; then
            if grep -q "SPRING_ELASTICSEARCH_URIS" "$TEMP_DEPLOYMENT" && (grep -q "127\.0\.0\.1:9200\|localhost:9200" "$TEMP_DEPLOYMENT"); then
                log_warn "⚠️  SPRING_ELASTICSEARCH_URIS에 127.0.0.1 또는 localhost가 남아있습니다!"
                log_warn "강제로 $DATA_EC2_IP:9200로 변경합니다."
                sed -i.bak "s|value: \".*127\.0\.0\.1:9200.*\"|value: \"$DATA_EC2_IP:9200\"|g" "$TEMP_DEPLOYMENT" 2>/dev/null || \
                sed "s|value: \".*127\.0\.0\.1:9200.*\"|value: \"$DATA_EC2_IP:9200\"|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp" && mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
                sed -i.bak "s|value: \".*localhost:9200.*\"|value: \"$DATA_EC2_IP:9200\"|g" "$TEMP_DEPLOYMENT" 2>/dev/null || \
                sed "s|value: \".*localhost:9200.*\"|value: \"$DATA_EC2_IP:9200\"|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp" && mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
                rm -f "${TEMP_DEPLOYMENT}.bak" 2>/dev/null || true
            fi
        fi
    fi
    
    # 임시 파일을 원본 위치에 복사 (kustomize가 읽을 수 있도록)
    # 매 배포마다 GitHub Actions가 최신 k8s 디렉토리를 복사하므로, 수정된 파일을 그대로 사용
    cp "$TEMP_DEPLOYMENT" "$DEPLOYMENT_FILE_TO_USE"
    rm -f "$TEMP_DEPLOYMENT"
    log_info "✅ 파일 업데이트 완료 (수정된 파일로 배포 예정): $DEPLOYMENT_FILE_TO_USE"
    log_info "📝 수정된 내용 확인:"
    grep -A 2 "SPRING_KAFKA_BOOTSTRAP_SERVERS\|SPRING_ELASTICSEARCH_URIS" "$DEPLOYMENT_FILE_TO_USE" || true
    
    # Deployment 또는 DaemonSet 파일 변수 설정 (나중에 사용)
    DEPLOYMENT_FILE="$DEPLOYMENT_FILE_TO_USE"
fi

# ===================================
# Settlement CronJob 패치 (baro-settlement 모듈만)
# ===================================
CRONJOB_FILE="$DEPLOY_PATH/cronjob.yaml"
if [[ "$DEPLOY_PATH" == *"baro-settlement"* ]] && [ -f "$CRONJOB_FILE" ]; then
    log_step "🔧 CronJob 파일 패치 중: $CRONJOB_FILE"
    TEMP_CRONJOB=$(mktemp)
    cp "$CRONJOB_FILE" "$TEMP_CRONJOB"
    if grep -q "CHANGE_ME_TO_EC2_IP" "$TEMP_CRONJOB"; then
        sed "s/CHANGE_ME_TO_EC2_IP/$DATA_EC2_IP/g" "$TEMP_CRONJOB" > "${TEMP_CRONJOB}.tmp"
        mv "${TEMP_CRONJOB}.tmp" "$TEMP_CRONJOB"
        cp "$TEMP_CRONJOB" "$CRONJOB_FILE"
        rm -f "$TEMP_CRONJOB"
        log_info "✅ CronJob IP 치환 완료: $DATA_EC2_IP"
    else
        rm -f "$TEMP_CRONJOB"
    fi
fi

# ===================================
# Deployment/DaemonSet 이름 추출 (selector 충돌 처리에 필요)
# ===================================
DEPLOYMENT_NAME=""
DAEMONSET_NAME=""
RESOURCE_TYPE=""

# DaemonSet 확인 (우선순위)
if [ -f "$DAEMONSET_FILE" ]; then
    RESOURCE_TYPE="DaemonSet"
    # daemonset.yaml에서 metadata.name 추출
    DAEMONSET_NAME=$(grep -E "^  name:" "$DAEMONSET_FILE" | head -1 | awk '{print $2}' 2>/dev/null)
    
    if [ -z "$DAEMONSET_NAME" ]; then
        DAEMONSET_NAME=$(grep -E "^\s+name:" "$DAEMONSET_FILE" | grep -v "namespace:" | head -1 | awk '{print $2}' 2>/dev/null)
    fi
    
    if [ -z "$DAEMONSET_NAME" ]; then
        DAEMONSET_NAME=$(sed -n '/^metadata:/,/^spec:/p' "$DAEMONSET_FILE" | grep "name:" | head -1 | awk '{print $2}' 2>/dev/null)
    fi
    
    if [ -z "$DAEMONSET_NAME" ] && [ -n "$APP_NAME" ]; then
        DAEMONSET_NAME="$APP_NAME"
        log_info "💡 DAEMONSET_NAME을 APP_NAME으로 설정: $DAEMONSET_NAME"
    fi
# Deployment 확인
elif [ -f "$DEPLOYMENT_FILE" ]; then
    RESOURCE_TYPE="Deployment"
    # deployment.yaml에서 metadata.name 추출 (여러 방법 시도)
    DEPLOYMENT_NAME=$(grep -E "^  name:" "$DEPLOYMENT_FILE" | head -1 | awk '{print $2}' 2>/dev/null)
    
    if [ -z "$DEPLOYMENT_NAME" ]; then
        DEPLOYMENT_NAME=$(grep -E "^\s+name:" "$DEPLOYMENT_FILE" | grep -v "namespace:" | head -1 | awk '{print $2}' 2>/dev/null)
    fi
    
    if [ -z "$DEPLOYMENT_NAME" ]; then
        DEPLOYMENT_NAME=$(sed -n '/^metadata:/,/^spec:/p' "$DEPLOYMENT_FILE" | grep "name:" | head -1 | awk '{print $2}' 2>/dev/null)
    fi
    
    if [ -z "$DEPLOYMENT_NAME" ] && [ -n "$APP_NAME" ]; then
        DEPLOYMENT_NAME="$APP_NAME"
        log_info "💡 DEPLOYMENT_NAME을 APP_NAME으로 설정: $DEPLOYMENT_NAME"
    fi
fi

# 최종 확인
if [ -z "$DAEMONSET_NAME" ] && [ -z "$DEPLOYMENT_NAME" ]; then
    log_warn "⚠️  리소스 이름을 추출할 수 없습니다. APP_NAME: $APP_NAME"
fi

if [ -n "$RESOURCE_TYPE" ]; then
    log_info "📋 리소스 타입: $RESOURCE_TYPE"
fi

# ===================================
# opa 배포 전 Eureka 및 OPA Bundle 확인
# ===================================
if [ "$MODULE_NAME" = "opa" ]; then
    log_step "⏳ OPA 배포 전 Eureka 준비 상태 확인 중..."
    if ! $KUBECTL_CMD wait --for=condition=ready pod -l app=eureka -n baro-prod --timeout=60s 2>&1; then
        log_warn "⚠️ Eureka Pod가 Ready 상태가 아닙니다. OPA 배포를 계속 진행하지만, initContainer에서 대기합니다."
    else
        log_info "✅ Eureka Pod가 Ready 상태입니다."
    fi
    
    log_step "⏳ OPA 배포 전 OPA Bundle 준비 상태 확인 중..."
    if ! $KUBECTL_CMD wait --for=condition=ready pod -l app=opa-bundle -n baro-prod --timeout=60s 2>&1; then
        log_warn "⚠️ OPA Bundle Pod가 Ready 상태가 아닙니다. OPA 배포를 계속 진행하지만, OPA가 번들을 가져올 수 없을 수 있습니다."
    else
        log_info "✅ OPA Bundle Pod가 Ready 상태입니다."
    fi
fi

# ===================================
# k8s 배포 (kustomize 사용)
# ===================================
log_step "📦 k8s 리소스 적용 중 (kustomize)..."
log_info "Applying resources from: $DEPLOY_PATH"
log_info "Data EC2 IP: $DATA_EC2_IP, Image Tag: $IMAGE_TAG"

# 배포 시도 (에러 출력을 변수에 저장)
log_info "📤 kubectl apply 실행 중..."
log_info "   명령: $KUBECTL_CMD apply -k $DEPLOY_PATH"

# set -e를 일시적으로 비활성화하여 에러를 캡처
set +e
APPLY_OUTPUT=$($KUBECTL_CMD apply -k "$DEPLOY_PATH" 2>&1)
APPLY_EXIT_CODE=$?
set -e

if [ $APPLY_EXIT_CODE -ne 0 ]; then
    log_error "❌ kubectl apply 실패 (exit code: $APPLY_EXIT_CODE)"
    log_error "출력:"
    echo "$APPLY_OUTPUT"
    # selector immutable 에러 확인
    if echo "$APPLY_OUTPUT" | grep -q "selector.*immutable\|field is immutable"; then
        if [ "$RESOURCE_TYPE" = "DaemonSet" ]; then
            log_warn "⚠️  DaemonSet selector 충돌 감지. 기존 DaemonSet을 삭제하고 재생성합니다..."
            
            if [ -z "$DAEMONSET_NAME" ] && [ -n "$APP_NAME" ]; then
                DAEMONSET_NAME="$APP_NAME"
            fi
            
            if [ -n "$DAEMONSET_NAME" ]; then
                log_info "기존 DaemonSet 삭제: $DAEMONSET_NAME"
                $KUBECTL_CMD delete daemonset "$DAEMONSET_NAME" -n baro-prod --ignore-not-found=true
                sleep 2
                log_info "DaemonSet 재생성 중..."
                
                set +e
                RECREATE_OUTPUT=$($KUBECTL_CMD apply -k "$DEPLOY_PATH" 2>&1)
                RECREATE_EXIT_CODE=$?
                set -e
                
                if [ $RECREATE_EXIT_CODE -eq 0 ]; then
                    log_info "✅ DaemonSet 재생성 완료"
                    echo "$RECREATE_OUTPUT"
                else
                    log_error "❌ DaemonSet 재생성 실패 (exit code: $RECREATE_EXIT_CODE)"
                    echo "$RECREATE_OUTPUT"
                    exit 1
                fi
            else
                log_error "❌ DaemonSet 이름을 찾을 수 없습니다. (APP_NAME: $APP_NAME)"
                log_error "수동으로 다음 명령어를 실행하세요:"
                log_error "  kubectl delete daemonset <daemonset-name> -n baro-prod"
                log_error "  kubectl apply -k $DEPLOY_PATH"
                exit 1
            fi
        else
            log_warn "⚠️  Deployment selector 충돌 감지. 기존 Deployment를 삭제하고 재생성합니다..."
            
            if [ -z "$DEPLOYMENT_NAME" ] && [ -n "$APP_NAME" ]; then
                DEPLOYMENT_NAME="$APP_NAME"
            fi
            
            if [ -n "$DEPLOYMENT_NAME" ]; then
                log_info "기존 Deployment 삭제: $DEPLOYMENT_NAME"
                $KUBECTL_CMD delete deployment "$DEPLOYMENT_NAME" -n baro-prod --ignore-not-found=true
                sleep 2
                log_info "Deployment 재생성 중..."
                
                set +e
                RECREATE_OUTPUT=$($KUBECTL_CMD apply -k "$DEPLOY_PATH" 2>&1)
                RECREATE_EXIT_CODE=$?
                set -e
                
                if [ $RECREATE_EXIT_CODE -eq 0 ]; then
                    log_info "✅ Deployment 재생성 완료"
                    echo "$RECREATE_OUTPUT"
                else
                    log_error "❌ Deployment 재생성 실패 (exit code: $RECREATE_EXIT_CODE)"
                    echo "$RECREATE_OUTPUT"
                    exit 1
                fi
            else
                log_error "❌ Deployment 이름을 찾을 수 없습니다. (APP_NAME: $APP_NAME)"
                log_error "수동으로 다음 명령어를 실행하세요:"
                log_error "  kubectl delete deployment <deployment-name> -n baro-prod"
                log_error "  kubectl apply -k $DEPLOY_PATH"
                exit 1
            fi
        fi
    else
        # 다른 종류의 에러
        echo "$APPLY_OUTPUT"
        log_error "❌ 배포 실패. 에러를 확인하세요."
        exit 1
    fi
else
    # 성공 시 출력
    echo "$APPLY_OUTPUT"
    log_info "✅ kubectl apply 성공!"
    log_info "📝 배포에 사용된 deployment.yaml의 환경 변수 확인:"
    if [ -f "$DEPLOYMENT_FILE" ]; then
        log_info "   SPRING_KAFKA_BOOTSTRAP_SERVERS:"
        grep -A 1 "SPRING_KAFKA_BOOTSTRAP_SERVERS" "$DEPLOYMENT_FILE" | grep "value:" || true
        log_info "   SPRING_ELASTICSEARCH_URIS:"
        grep -A 1 "SPRING_ELASTICSEARCH_URIS" "$DEPLOYMENT_FILE" | grep "value:" || true
    fi
    
    # unchanged가 나왔지만 deployment.yaml이 수정되었는지 확인
    DEPLOYMENT_WAS_MODIFIED=false
    if echo "$APPLY_OUTPUT" | grep -q "unchanged"; then
        log_info "🔍 kubectl apply에서 'unchanged' 감지됨"
        # deployment.yaml이 수정되었는지 확인 (127.0.0.1이나 localhost가 없고 DATA_EC2_IP가 있는지)
        if [ -f "$DEPLOYMENT_FILE" ] && [ -n "$DATA_EC2_IP" ]; then
            # Kafka나 Elasticsearch 설정에 DATA_EC2_IP가 포함되어 있고, 127.0.0.1이나 localhost가 없으면 수정된 것
            if grep -q "SPRING_KAFKA_BOOTSTRAP_SERVERS" "$DEPLOYMENT_FILE" && \
               grep "SPRING_KAFKA_BOOTSTRAP_SERVERS" "$DEPLOYMENT_FILE" | grep -q "$DATA_EC2_IP" && \
               ! grep "SPRING_KAFKA_BOOTSTRAP_SERVERS" "$DEPLOYMENT_FILE" | grep -qE "127\.0\.0\.1|localhost"; then
                DEPLOYMENT_WAS_MODIFIED=true
                log_info "✅ deployment.yaml이 수정되었습니다 (Kafka: $DATA_EC2_IP 사용)"
            fi
            if grep -q "SPRING_ELASTICSEARCH_URIS" "$DEPLOYMENT_FILE" && \
               grep "SPRING_ELASTICSEARCH_URIS" "$DEPLOYMENT_FILE" | grep -q "$DATA_EC2_IP" && \
               ! grep "SPRING_ELASTICSEARCH_URIS" "$DEPLOYMENT_FILE" | grep -qE "127\.0\.0\.1|localhost"; then
                DEPLOYMENT_WAS_MODIFIED=true
                log_info "✅ deployment.yaml이 수정되었습니다 (Elasticsearch: $DATA_EC2_IP 사용)"
            fi
            # CHANGE_ME_TO_EC2_IP가 없고 DATA_EC2_IP가 있으면 수정된 것
            if ! grep -q "CHANGE_ME_TO_EC2_IP" "$DEPLOYMENT_FILE" && grep -q "$DATA_EC2_IP" "$DEPLOYMENT_FILE"; then
                DEPLOYMENT_WAS_MODIFIED=true
                log_info "✅ deployment.yaml이 수정되었습니다 (IP 치환 완료)"
            fi
        fi
        
        if [ "$DEPLOYMENT_WAS_MODIFIED" = true ]; then
            log_warn "⚠️  deployment.yaml이 수정되었지만 kubectl apply가 'unchanged'로 표시했습니다."
            log_warn "⚠️  변경사항을 적용하기 위해 강제로 Pod를 재시작합니다."
        fi
    fi
fi

# ===================================
# Pod 재시작 (rollout restart)
# ===================================
# 1. IMAGE_TAG가 latest일 때
# 2. 또는 deployment.yaml이 수정되었는데 unchanged가 나왔을 때
if [ -n "$DAEMONSET_NAME" ]; then
    if [ "$IMAGE_TAG" = "latest" ]; then
        log_info "🔄 latest 태그 사용 중이므로 Pod 재시작 (rollout restart)..."
        $KUBECTL_CMD rollout restart daemonset/"$DAEMONSET_NAME" -n baro-prod || true
    fi
elif [ -n "$DEPLOYMENT_NAME" ]; then
    if [ "$IMAGE_TAG" = "latest" ]; then
        log_info "🔄 latest 태그 사용 중이므로 Pod 재시작 (rollout restart)..."
        $KUBECTL_CMD rollout restart deployment/"$DEPLOYMENT_NAME" -n baro-prod || true
    elif [ "$DEPLOYMENT_WAS_MODIFIED" = true ]; then
        log_info "🔄 deployment.yaml 변경사항 적용을 위해 Pod 재시작 (rollout restart)..."
        $KUBECTL_CMD rollout restart deployment/"$DEPLOYMENT_NAME" -n baro-prod || true
    fi
fi

# ===================================
# 배포 상태 확인
# ===================================
if [ -f "$DAEMONSET_FILE" ]; then
    if [ -n "$DAEMONSET_NAME" ]; then
        log_step "⏳ Pod가 Ready 상태가 될 때까지 대기 중..."
        if ! $KUBECTL_CMD wait --for=condition=ready pod -l app="$APP_NAME" -n baro-prod --timeout=300s 2>&1; then
            log_warn "⚠️ $APP_NAME Pod가 Ready 상태가 되지 않았습니다. 상태 확인 중..."
            echo ""
            echo "📊 $APP_NAME Pod 상태:"
            $KUBECTL_CMD get pods -n baro-prod -l app="$APP_NAME" 2>&1 || true
            echo ""
            echo "📋 $APP_NAME DaemonSet 상태:"
            $KUBECTL_CMD get daemonset "$DAEMONSET_NAME" -n baro-prod 2>&1 || true
            echo ""
            echo "📝 $APP_NAME Pod 이벤트:"
            LATEST_POD=$($KUBECTL_CMD get pods -n baro-prod -l app="$APP_NAME" --sort-by=.metadata.creationTimestamp -o jsonpath='{.items[-1].metadata.name}' 2>/dev/null || echo "")
            if [ -n "$LATEST_POD" ]; then
                $KUBECTL_CMD describe pod "$LATEST_POD" -n baro-prod 2>&1 | grep -A 30 "Events:" || true
                echo ""
                echo "📄 $APP_NAME Pod 로그 (마지막 50줄):"
                $KUBECTL_CMD logs "$LATEST_POD" -n baro-prod --tail=50 2>&1 || true
            fi
            log_warn "⚠️ $APP_NAME 배포는 계속 진행하지만, Pod가 준비되지 않았을 수 있습니다."
            log_warn "💡 로그 확인 명령어: kubectl logs -n baro-prod -l app=$APP_NAME --tail=100"
        else
            log_info "✅ $APP_NAME Pod가 Ready 상태입니다."
        fi
        
        log_info "✅ 배포 완료: $MODULE_NAME"
        $KUBECTL_CMD get pods -n baro-prod -l app="$APP_NAME"
    else
        log_info "✅ 리소스 적용 완료: $MODULE_NAME"
    fi
elif [ -f "$DEPLOYMENT_FILE" ]; then
    DEPLOYMENT_NAME=$(grep -E "^  name:" "$DEPLOYMENT_FILE" | head -1 | awk '{print $2}' || echo "")
    if [ -n "$DEPLOYMENT_NAME" ]; then
        log_step "⏳ Pod가 Ready 상태가 될 때까지 대기 중..."
        if ! $KUBECTL_CMD wait --for=condition=ready pod -l app="$APP_NAME" -n baro-prod --timeout=300s 2>&1; then
            log_warn "⚠️ $APP_NAME Pod가 Ready 상태가 되지 않았습니다. 상태 확인 중..."
            echo ""
            echo "📊 $APP_NAME Pod 상태:"
            $KUBECTL_CMD get pods -n baro-prod -l app="$APP_NAME" 2>&1 || true
            echo ""
            echo "📋 $APP_NAME Deployment 상태:"
            $KUBECTL_CMD get deployment "$DEPLOYMENT_NAME" -n baro-prod 2>&1 || true
            echo ""
            echo "📝 $APP_NAME Pod 이벤트:"
            LATEST_POD=$($KUBECTL_CMD get pods -n baro-prod -l app="$APP_NAME" --sort-by=.metadata.creationTimestamp -o jsonpath='{.items[-1].metadata.name}' 2>/dev/null || echo "")
            if [ -n "$LATEST_POD" ]; then
                $KUBECTL_CMD describe pod "$LATEST_POD" -n baro-prod 2>&1 | grep -A 30 "Events:" || true
                echo ""
                echo "📄 $APP_NAME Pod 로그 (마지막 50줄):"
                $KUBECTL_CMD logs "$LATEST_POD" -n baro-prod --tail=50 2>&1 || true
            fi
            log_warn "⚠️ $APP_NAME 배포는 계속 진행하지만, Pod가 준비되지 않았을 수 있습니다."
            log_warn "💡 로그 확인 명령어: kubectl logs -n baro-prod -l app=$APP_NAME --tail=100"
        else
            log_info "✅ $APP_NAME Pod가 Ready 상태입니다."
        fi
        
        log_info "✅ 배포 완료: $MODULE_NAME"
        $KUBECTL_CMD get pods -n baro-prod -l app="$APP_NAME"
    else
        log_info "✅ 리소스 적용 완료: $MODULE_NAME"
    fi
else
    log_info "✅ 리소스 적용 완료: $MODULE_NAME"
fi

log_info "🎉 Deployment completed!"
