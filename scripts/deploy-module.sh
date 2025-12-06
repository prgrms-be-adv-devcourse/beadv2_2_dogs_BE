#!/bin/bash

# ===================================
# 모듈별 배포 스크립트
# Usage: bash deploy-module.sh [MODULE_NAME]
# Example: bash deploy-module.sh auth
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
# Docker Compose 명령어 감지
# ===================================
detect_docker_compose() {
    if command -v docker-compose &> /dev/null; then
        echo "docker-compose"
    elif docker compose version &> /dev/null; then
        echo "docker compose"
    else
        log_error "Docker Compose not found. Please install Docker Compose."
        exit 1
    fi
}

DOCKER_COMPOSE=$(detect_docker_compose)
log_info "Using Docker Compose command: $DOCKER_COMPOSE"

# ===================================
# 파라미터 검증
# ===================================
MODULE_NAME=$1

if [ -z "$MODULE_NAME" ]; then
    log_error "모듈 이름을 지정해주세요."
    echo "Usage: bash deploy-module.sh [MODULE_NAME]"
    echo ""
    echo "Available modules:"
    echo "  - data    (데이터 인프라: Redis, MySQL, Kafka)"
    echo "  - cloud   (Spring Cloud: Eureka, Gateway, Config)"
    echo "  - infra   (data + cloud)"
    echo "  - auth    (인증 모듈)"
    echo "  - buyer   (구매자 모듈)"
    echo "  - seller  (판매자 모듈)"
    echo "  - order   (주문 모듈)"
    echo "  - support (지원 모듈)"
    echo "  - all     (전체 배포)"
    exit 1
fi

# ===================================
# 환경 변수
# ===================================
GITHUB_USERNAME="${GITHUB_USERNAME:-do-develop-space}"
PROJECT_DIR="${HOME}/apps/BE"

# 디렉토리 생성 (없으면)
mkdir -p ${PROJECT_DIR}

cd ${PROJECT_DIR}

log_info "🚀 Deploying module: ${MODULE_NAME}"

# ===================================
# 1. GitHub Container Registry 로그인
# ===================================
log_step "📦 Logging in to GitHub Container Registry..."
if [ -n "${GITHUB_TOKEN}" ]; then
    echo "${GITHUB_TOKEN}" | docker login ghcr.io -u "${GITHUB_USERNAME}" --password-stdin
else
    log_warn "GITHUB_TOKEN not set, skipping registry login"
fi

# ===================================
# 2. 인프라 서비스 확인
# ===================================
check_data_infra() {
    log_step "🔍 Checking data infrastructure..."
    if ! docker ps | grep -q baro-redis; then
        log_warn "Data infrastructure not running. Starting data infrastructure first..."
        $DOCKER_COMPOSE -f docker-compose.data.yml pull
        $DOCKER_COMPOSE -f docker-compose.data.yml up -d
        log_info "Waiting for data infrastructure to be ready (20 seconds)..."
        sleep 20
    else
        log_info "Data infrastructure is already running."
    fi
}

check_cloud_infra() {
    log_step "🔍 Checking Spring Cloud infrastructure..."
    if ! docker ps | grep -q baro-eureka; then
        log_warn "Spring Cloud infrastructure not running. Starting cloud infrastructure first..."
        $DOCKER_COMPOSE -f docker-compose.cloud.yml pull
        $DOCKER_COMPOSE -f docker-compose.cloud.yml up -d
        log_info "Waiting for Spring Cloud to be ready (30 seconds)..."
        sleep 30
    else
        log_info "Spring Cloud infrastructure is already running."
    fi
}

# ===================================
# 3. 모듈별 배포
# ===================================
deploy_module() {
    local module=$1
    local compose_file="docker-compose.${module}.yml"
    
    if [ ! -f "$compose_file" ]; then
        log_error "Compose file not found: $compose_file"
        exit 1
    fi
    
    # 현재 버전 기록 (롤백용)
    CURRENT_IMAGE=$(docker inspect "baro-${module}" --format='{{.Config.Image}}' 2>/dev/null || echo "none")
    
    log_step "📥 Pulling latest image for $module..."
    $DOCKER_COMPOSE -f "$compose_file" pull
    
    # Pull한 이미지 정보
    NEW_IMAGE=$($DOCKER_COMPOSE -f "$compose_file" config | grep "image:" | head -1 | awk '{print $2}')
    
    log_step "🛑 Stopping existing container for $module..."
    $DOCKER_COMPOSE -f "$compose_file" down || true
    
    log_step "🏃 Starting $module..."
    $DOCKER_COMPOSE -f "$compose_file" up -d
    
    # 배포 이력 저장
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Deploy: $module | Previous: $CURRENT_IMAGE | New: $NEW_IMAGE" >> ${PROJECT_DIR}/deployment-history.log
    
    log_info "✅ Module $module deployed successfully!"
    log_info "📝 Deployment recorded in ${PROJECT_DIR}/deployment-history.log"
}

# ===================================
# 4. 전체 배포
# ===================================
deploy_all() {
    log_step "Deploying all modules..."
    
    # 1. 데이터 인프라
    log_info "Step 1/4: Deploying data infrastructure..."
    $DOCKER_COMPOSE -f docker-compose.data.yml pull
    $DOCKER_COMPOSE -f docker-compose.data.yml up -d
    sleep 20
    
    # 2. Spring Cloud 인프라
    log_info "Step 2/4: Deploying Spring Cloud infrastructure..."
    $DOCKER_COMPOSE -f docker-compose.cloud.yml pull
    $DOCKER_COMPOSE -f docker-compose.cloud.yml up -d
    sleep 30
    
    # 3. 비즈니스 모듈들
    log_info "Step 3/4: Deploying business modules..."
    for module in auth buyer seller order support; do
        deploy_module "$module"
        sleep 5
    done
    
    log_info "✅ All modules deployed successfully!"
}

# ===================================
# 메인 로직
# ===================================
case $MODULE_NAME in
    data)
        log_step "Deploying data infrastructure..."
        $DOCKER_COMPOSE -f docker-compose.data.yml pull
        $DOCKER_COMPOSE -f docker-compose.data.yml down || true
        $DOCKER_COMPOSE -f docker-compose.data.yml up -d
        log_info "✅ Data infrastructure deployed successfully!"
        ;;
    
    cloud)
        log_step "Deploying Spring Cloud infrastructure..."
        check_data_infra
        $DOCKER_COMPOSE -f docker-compose.cloud.yml pull
        $DOCKER_COMPOSE -f docker-compose.cloud.yml down || true
        $DOCKER_COMPOSE -f docker-compose.cloud.yml up -d
        log_info "✅ Spring Cloud infrastructure deployed successfully!"
        ;;
    
    # infra)
    #     log_step "Deploying all infrastructure (data + cloud)..."
    #     $DOCKER_COMPOSE -f docker-compose.data.yml pull
    #     $DOCKER_COMPOSE -f docker-compose.data.yml up -d
    #     sleep 20
    #     $DOCKER_COMPOSE -f docker-compose.cloud.yml pull
    #     $DOCKER_COMPOSE -f docker-compose.cloud.yml up -d
    #     log_info "✅ All infrastructure deployed successfully!"
    #     ;;
    
    auth|buyer|seller|order|support)
        check_data_infra
        check_cloud_infra
        deploy_module "$MODULE_NAME"
        ;;
    
    # all)
    #     deploy_all
    #     ;;
    
    *)
        log_error "Unknown module: $MODULE_NAME"
        log_info "Available modules: data, cloud, infra, auth, buyer, seller, order, support"
        log_info "Unavailable modules: infra, all"
        exit 1
        ;;
esac

# ===================================
# 5. 상태 확인
# ===================================
log_step "🔍 Checking container status..."
docker ps --filter "name=baro-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# ===================================
# 6. 정리
# ===================================
log_step "🧹 Cleaning up unused Docker resources..."
docker system prune -f --volumes

log_info "🎉 Deployment completed!"

