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
    echo "  - data    (데이터 인프라: MySQL, Kafka, Elasticsearch - docker-compose로 배포)"
    exit 1
fi

# ===================================
# k8s 디렉토리 자동 탐색
# ===================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_BASE_DIR=""

# 여러 경로에서 k8s 디렉토리 찾기 (우선순위 순)
if [ -d "/home/ubuntu/apps/k8s/cloud" ]; then
    # 배포 기준 디렉토리 (최우선)
    K8S_BASE_DIR="/home/ubuntu/apps/k8s"
elif [ -d "$SCRIPT_DIR/../k8s/cloud" ]; then
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
        log_info "사용 가능한 모듈: cloud, eureka, config, gateway, redis, auth, buyer, seller, order, support, data"
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
# Deployment 파일에 EC2 IP 및 이미지 태그 설정 (임시 파일 사용)
# ===================================
DEPLOYMENT_FILE="$DEPLOY_PATH/deployment.yaml"
TEMP_DEPLOYMENT=""

if [ -f "$DEPLOYMENT_FILE" ]; then
    # 임시 파일 생성 (원본 파일 보존)
    TEMP_DEPLOYMENT=$(mktemp)
    cp "$DEPLOYMENT_FILE" "$TEMP_DEPLOYMENT"
    
    log_step "🔧 Deployment 파일 설정 중..."
    
    # hostNetwork 여부 확인
    USE_HOST_NETWORK=false
    if grep -q "hostNetwork: true" "$TEMP_DEPLOYMENT"; then
        USE_HOST_NETWORK=true
        log_info "🌐 hostNetwork: true 감지 - localhost(127.0.0.1) 사용"
    else
        log_info "🌐 hostNetwork: false - EC2 IP($EC2_IP) 사용"
    fi
    
    # EC2 IP 설정 (hostNetwork 여부에 따라 다르게 처리)
    # hostNetwork: true인 경우 -> 127.0.0.1 사용
    # hostNetwork: false인 경우 -> EC2 IP 사용
    
    if grep -q "CHANGE_ME_TO_EC2_IP" "$TEMP_DEPLOYMENT"; then
        if [ "$USE_HOST_NETWORK" = "true" ]; then
            # hostNetwork: true인 경우 127.0.0.1로 치환
            REPLACEMENT_IP="127.0.0.1"
            log_info "EC2 IP 설정 중 (hostNetwork: true -> CHANGE_ME_TO_EC2_IP -> 127.0.0.1)"
        else
            # hostNetwork: false인 경우 EC2 IP로 치환
            REPLACEMENT_IP="$EC2_IP"
            log_info "EC2 IP 설정 중 (hostNetwork: false -> CHANGE_ME_TO_EC2_IP -> $EC2_IP)"
        fi
        
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
    
    # EC2_IP 환경 변수 설정 (hostNetwork 여부에 따라)
    if grep -q "name: EC2_IP" "$TEMP_DEPLOYMENT"; then
        if [ "$USE_HOST_NETWORK" = "true" ]; then
            # hostNetwork: true인 경우 EC2_IP 환경 변수도 127.0.0.1로 설정
            if grep -q 'value: "CHANGE_ME_TO_EC2_IP"' "$TEMP_DEPLOYMENT" || grep -q 'value: "127.0.0.1"' "$TEMP_DEPLOYMENT" || grep -q "value: \"$EC2_IP\"" "$TEMP_DEPLOYMENT"; then
                log_info "EC2_IP 환경 변수 설정 중 (hostNetwork: true -> 127.0.0.1)"
                sed "/name: EC2_IP/,/value:/ s|value: \".*\"|value: \"127.0.0.1\"|" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
                mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
            fi
        else
            # hostNetwork: false인 경우 EC2 IP로 설정
            if grep -q 'value: "CHANGE_ME_TO_EC2_IP"' "$TEMP_DEPLOYMENT" || grep -q 'value: "127.0.0.1"' "$TEMP_DEPLOYMENT"; then
                log_info "EC2_IP 환경 변수 설정 중 (hostNetwork: false -> $EC2_IP)"
                sed "/name: EC2_IP/,/value:/ s|value: \".*\"|value: \"$EC2_IP\"|" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
                mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
            fi
        fi
    fi
    
    # SPRING_DATASOURCE_URL에서 $(EC2_IP) 패턴 처리
    # hostNetwork: true일 때는 127.0.0.1을 그대로 유지
    if grep -q "SPRING_DATASOURCE_URL" "$TEMP_DEPLOYMENT"; then
        log_info "SPRING_DATASOURCE_URL 업데이트 중 (EC2 IP: $EC2_IP, hostNetwork: $USE_HOST_NETWORK)"
        # $(EC2_IP) 패턴을 실제 IP로 변경
        sed "s|\$(EC2_IP)|$EC2_IP|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
        mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
        
        # hostNetwork: false인 경우에만 127.0.0.1:3306을 EC2 IP로 변경
        # hostNetwork: true인 경우 127.0.0.1은 그대로 유지해야 함
        if [ "$USE_HOST_NETWORK" = "false" ]; then
            # 127.0.0.1:3306 패턴을 실제 IP로 변경 (데이터베이스 URL만)
            sed "s|127\.0\.0\.1:3306|$EC2_IP:3306|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
            mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
            log_info "SPRING_DATASOURCE_URL: 127.0.0.1:3306 -> $EC2_IP:3306 (hostNetwork: false)"
        else
            log_info "SPRING_DATASOURCE_URL: 127.0.0.1:3306 유지 (hostNetwork: true)"
        fi
    fi
    
    # 이미지 태그 업데이트 (latest가 아니고 변경이 필요한 경우)
    if [ "$IMAGE_TAG" != "latest" ]; then
        log_step "🏷️  이미지 태그 업데이트: $IMAGE_TAG"
        SERVICE_NAME=$(grep -E "image:" "$TEMP_DEPLOYMENT" | head -1 | sed -E 's/.*image:.*\/([^:]+):.*/\1/')
        if [ -n "$SERVICE_NAME" ]; then
            sed "s|ghcr.io/do-develop-space/${SERVICE_NAME}:latest|ghcr.io/do-develop-space/${SERVICE_NAME}:${IMAGE_TAG}|g" "$TEMP_DEPLOYMENT" > "${TEMP_DEPLOYMENT}.tmp"
            mv "${TEMP_DEPLOYMENT}.tmp" "$TEMP_DEPLOYMENT"
            log_info "✅ 이미지 태그 업데이트 완료: ${SERVICE_NAME}:${IMAGE_TAG}"
        fi
    fi
else
    log_error "Deployment 파일을 찾을 수 없습니다: $DEPLOYMENT_FILE"
    exit 1
fi

# ===================================
# k8s 배포
# ===================================
log_step "📦 k8s 리소스 적용 중..."

# Service는 원본 파일 사용
if [ -f "$DEPLOY_PATH/service.yaml" ]; then
    log_info "Applying Service..."
    $KUBECTL_CMD apply -f "$DEPLOY_PATH/service.yaml"
fi

# Deployment는 임시 파일 사용 (EC2 IP 및 이미지 태그가 적용된 버전)
if [ -n "$TEMP_DEPLOYMENT" ] && [ -f "$TEMP_DEPLOYMENT" ]; then
    # 최종 검증: CHANGE_ME_TO_EC2_IP가 남아있는지 확인
    if grep -q "CHANGE_ME_TO_EC2_IP" "$TEMP_DEPLOYMENT"; then
        log_error "❌ 치환되지 않은 CHANGE_ME_TO_EC2_IP가 남아있습니다!"
        log_error "다음 위치에서 발견:"
        grep -n "CHANGE_ME_TO_EC2_IP" "$TEMP_DEPLOYMENT" || true
        log_error "배포를 중단합니다. 스크립트를 확인하세요."
        rm -f "$TEMP_DEPLOYMENT"
        exit 1
    fi
    
    log_info "Applying Deployment (EC2 IP: $EC2_IP, Image Tag: $IMAGE_TAG)..."
    $KUBECTL_CMD apply -f "$TEMP_DEPLOYMENT"
    # 임시 파일 삭제
    rm -f "$TEMP_DEPLOYMENT"
else
    # 임시 파일이 없으면 원본 사용
    log_warn "⚠️  임시 파일이 없습니다. 원본 파일을 사용합니다."
    $KUBECTL_CMD apply -f "$DEPLOY_PATH/"
fi

# ===================================
# 배포 상태 확인
# ===================================
if [ -f "$DEPLOYMENT_FILE" ]; then
    DEPLOYMENT_NAME=$(grep -E "^  name:" "$DEPLOYMENT_FILE" | head -1 | awk '{print $2}' || echo "")
    if [ -n "$DEPLOYMENT_NAME" ]; then
        log_step "⏳ Pod가 Ready 상태가 될 때까지 대기 중..."
        $KUBECTL_CMD wait --for=condition=ready pod -l app="$APP_NAME" -n baro-prod --timeout=300s || {
            log_warn "Pod가 Ready 상태가 되지 않았습니다. 상태를 확인합니다..."
            $KUBECTL_CMD get pods -n baro-prod -l app="$APP_NAME"
            
            # 가장 최근 Pod의 로그 출력
            LATEST_POD=$($KUBECTL_CMD get pods -n baro-prod -l app="$APP_NAME" --sort-by=.metadata.creationTimestamp -o jsonpath='{.items[-1].metadata.name}' 2>/dev/null)
            if [ -n "$LATEST_POD" ]; then
                log_warn "📋 Pod 로그 ($LATEST_POD):"
                $KUBECTL_CMD logs -n baro-prod "$LATEST_POD" --tail=50 || true
                
                log_warn "📊 Pod 이벤트:"
                $KUBECTL_CMD describe pod -n baro-prod "$LATEST_POD" | grep -A 20 "Events:" || true
            fi
            
            log_warn "💡 로그 확인 명령어: kubectl logs -n baro-prod -l app=$APP_NAME --tail=100"
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
